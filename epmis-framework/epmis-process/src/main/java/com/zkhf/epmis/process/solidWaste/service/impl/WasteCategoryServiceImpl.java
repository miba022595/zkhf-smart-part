package com.zkhf.epmis.process.solidWaste.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.EntInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.solidWaste.WasteCategoryMapper;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategory;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategoryReq;
import com.zkhf.epmis.process.solidWaste.domain.WasteDict;
import com.zkhf.epmis.process.solidWaste.service.WasteCategoryService;
import com.zkhf.epmis.process.solidWaste.service.WasteDictService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 固废种类管理Service业务层处理
 */
@Slf4j
@Service
public class WasteCategoryServiceImpl implements WasteCategoryService {

    private WasteCategoryMapper wasteCategoryMapper;
    @Autowired
    public void setTWasteCategoryMapper(WasteCategoryMapper wasteCategoryMapper) {
        this.wasteCategoryMapper = wasteCategoryMapper;
    }

    private WasteDictService wasteDictService;
    @Autowired
    public void setWasteDictService(WasteDictService wasteDictService) {
        this.wasteDictService = wasteDictService;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public WasteCategory selectWasteCategoryById(String categoryId) {
        return wasteCategoryMapper.selectWasteCategoryById(categoryId);
    }

    @Override
    public AjaxResult selectWasteCategoryList(WasteCategoryReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 固废种类管理列表查询
        boolean page = PageUtils.startPageCheckExists();
        List<WasteCategory> list = wasteCategoryMapper.selectWasteCategoryList(req);
        // 填充信息
        fill(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "固废种类管理", businessType = BusinessType.EXPORT)
    public void exportWasteCategory(WasteCategoryReq req, HttpServletResponse response) {
        // 请求参数转换
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("固废种类列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<WasteCategory> list = null;
            if (null != req) {
                list = wasteCategoryMapper.selectWasteCategoryList(req);
            }
            if (null != list && !list.isEmpty()) {
                // 填充内容
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 从第2行开始插入
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN6 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,6);
                Row row;
                int index = 1;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (WasteCategory room : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, styleN0);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, room.getEntName(), style);
                    // 归属排口
                    CellUtils.setStringVal(row, cellIndex++, room.getOutPutCode(), style);
                    // 归属排口
                    CellUtils.setStringVal(row, cellIndex++, room.getOutPutName(), style);
                    // 固废分类
                    CellUtils.setStringVal(row, cellIndex++, room.getWasteCategory(), style);
                    // 固废类别
                    CellUtils.setStringVal(row, cellIndex++, room.getWasteType(), style);
                    // 固废代码
                    CellUtils.setStringVal(row, cellIndex++, room.getWasteCode(), style);
                    // 废物名称(具体名称)
                    CellUtils.setStringVal(row, cellIndex++, room.getWasteName(), style);
                    // 处置/处理方法
                    CellUtils.setStringVal(row, cellIndex++, room.getDisposalDesc(), style);
                    // 废物形态
                    CellUtils.setStringVal(row, cellIndex++, room.getWasteFormDesc(), style);
                    // 设计生产量(t/a)
                    CellUtils.setDoubleVal(row, cellIndex++, room.getDesignOutput(), styleN6);
                    // 容器/包装类型
                    CellUtils.setStringVal(row, cellIndex++, room.getPackageTypeDesc(), style);
                    // 主要成分
                    CellUtils.setStringVal(row, cellIndex++, room.getMainComponent(), style);
                    // 有害成分(危废专用)
                    CellUtils.setStringVal(row, cellIndex++, room.getHazardousComponent(), style);
                    // 危险特性
                    CellUtils.setStringVal(row, cellIndex++, room.getHazardCharacteristicDesc(), style);
                    // 注意事项（从字典上取到值，逗号拼接成字符串，可供修改）
                    CellUtils.setStringVal(row, cellIndex++, room.getPrecautionDesc(), style);
                    // 应急措施
                    CellUtils.setStringVal(row, cellIndex++, room.getEmergencyMeasures(), style);
                    // 委托处置单位(第三方单位)
                    CellUtils.setStringVal(row, cellIndex, room.getDisposalUnitName(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("固废种类列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("outputStream close", e);
                }
            }
        }
    }

    private void fill(List<WasteCategory> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 转换分类信息
        List<WasteDict> districts = wasteDictService.selectWasteDictList(null);
        Map<String, WasteDict> districtMap = new HashMap<>();
        districts.forEach( e -> districtMap.put(e.getId() + "", e));
        /*
        disposalMethod: 处置/处理类型, 字典 operation_category
        wasteForm: 废物形态类型，字典  waste_form
        packageType: 容器/包装类型, 字典 container_type
        hazardCharacteristic: 危险特性，字典  hazard_characteristic
        precautions: 固废注意事项，字典  waste_precautions
        */
        List<String> dictTypes = Arrays.asList("operation_category", "waste_form", "container_type", "hazard_characteristic", "waste_precautions");
        Map<String, Map<String, String>> dictMap = platformFacade.selectDataMapByTypes(dictTypes);
        List<EntInfo> entList = redisCacheUtils.getAllEntList();
        Map<String, String> entMap = new HashMap<>();
        if (null != entList && !entList.isEmpty()) {
            entList.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getEntCode())) {
                    entMap.put(e.getEntCode(), e.getEntName());
                }
            });
        }
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        Map<String, OutPutInfo> outMap = new HashMap<>();
        if (null != outList && !outList.isEmpty()) {
            outList.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getOutPutId())) {
                    outMap.put(e.getOutPutId(), e);
                }
            });
        }
        // 获取第三方单位
        List<Map<String, Object>> extList = platformFacade.allExtUnitList();
        Map<String, String> extMap = new HashMap<>();
        if (null != extList && !extList.isEmpty()) {
            extList.forEach( u -> {
                String unitCode = MapUtil.getStr(u, "unitCode");
                if (StringUtils.isNotEmpty(unitCode)) {
                    extMap.put(unitCode, MapUtil.getStr(u, "unitName"));
                }
            });
        }
        list.forEach( e -> {
            e.setEntName(entMap.get(e.getEntCode()));
            OutPutInfo out = outMap.get(e.getOutPutId());
            if (null != out) {
                e.setOutPutCode(out.getOutPutCode());
                e.setOutPutName(out.getOutPutName());
            }
            e.setDisposalUnitName(extMap.get(e.getDisposalUnit()));
            Map<String, String> sub = dictMap.get("operation_category");
            if (null != sub && sub.containsKey(e.getDisposalMethod())) {
                e.setDisposalDesc(sub.get(e.getDisposalMethod()));
            }
            sub = dictMap.get("waste_form");
            if (null != sub && sub.containsKey(e.getWasteForm())) {
                e.setWasteFormDesc(sub.get(e.getWasteForm()));
            }
            sub = dictMap.get("container_type");
            if (null != sub && sub.containsKey(e.getPackageType())) {
                e.setPackageTypeDesc(sub.get(e.getPackageType()));
            }
            sub = dictMap.get("hazard_characteristic");
            if (null != sub && StringUtils.isNotEmpty(e.getHazardCharacteristic())) {
                String item;
                for (String k : e.getHazardCharacteristic().split(",")) {
                    item = sub.get(k);
                    if (StringUtils.isEmpty(item)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(e.getHazardCharacteristicDesc())) {
                        e.setHazardCharacteristicDesc(item);
                    } else {
                        e.setHazardCharacteristicDesc(e.getHazardCharacteristicDesc() + "，" + item);
                    }
                }
            }
            sub = dictMap.get("waste_precautions");
            if (null != sub && StringUtils.isNotEmpty(e.getPrecautions())) {
                String item;
                for (String k : e.getPrecautions().split(",")) {
                    item = sub.get(k);
                    if (StringUtils.isEmpty(item)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(e.getPrecautionDesc())) {
                        e.setPrecautionDesc(item);
                    } else {
                        e.setPrecautionDesc(e.getPrecautionDesc() + "，" + item);
                    }
                }
            }
            if (StringUtils.isNotEmpty(e.getWasteDictId())) {
                String[] ids = e.getWasteDictId().split(",");
                WasteDict dict = districtMap.get(ids[0]);
                if (null != dict) {
                    e.setWasteCategory(dict.getName());
                    dict = districtMap.get(ids[ids.length - 1]);
                    if (null != dict) {
                        e.setWasteType(dict.getName());
                        e.setWasteCode(dict.getTag());
                    }
                }
            }
        });
    }

    private WasteCategoryReq initReq(WasteCategoryReq req) {
        if (null == req) {
            req = new WasteCategoryReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return null;
            }
            if (StringUtils.isNotEmpty(req.getEntCode()) && !req.getEntCodes().contains(req.getEntCode())) {
                return null;
            }
        }
        return req;
    }

    @Override
    @Log(title = "固废种类管理", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteCategory(WasteCategory info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        info.setCategoryId(UlidCreator.getMonotonicUlid().toString());
        int count = wasteCategoryMapper.insertWasteCategory(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废种类管理", businessType = BusinessType.UPDATE)
    public AjaxResult updateWasteCategory(WasteCategory info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        // 判断是否被使用
        int count = wasteCategoryMapper.selectUsedSizeCategory(info.getCategoryId());
        if (count > 0) {
            return AjaxResult.error("该种类已被使用，不能修改");
        }
        count = wasteCategoryMapper.selectUsedSizeFromPlan(info.getCategoryId());
        if (count > 0) {
            return AjaxResult.error("该种类已存在计划，不能修改");
        }
        count = wasteCategoryMapper.updateWasteCategory(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废种类管理", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteCategory(WasteCategory info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        WasteCategory category = wasteCategoryMapper.selectWasteCategoryById(info.getCategoryId());
        if (null == category) {
            return AjaxResult.error("无该种类");
        }
        // 判断是否被使用
        int count = wasteCategoryMapper.selectUsedSizeCategory(category.getCategoryId());
        if (count > 0) {
            return AjaxResult.error("该种类已被使用，不能删除");
        }
        count = wasteCategoryMapper.selectUsedSizeFromPlan(info.getCategoryId());
        if (count > 0) {
            return AjaxResult.error("该种类已存在计划，不能删除");
        }
        count = wasteCategoryMapper.deleteWasteCategoryByCategoryId(category.getCategoryId());
        return AjaxResult.success(count);
    }
}


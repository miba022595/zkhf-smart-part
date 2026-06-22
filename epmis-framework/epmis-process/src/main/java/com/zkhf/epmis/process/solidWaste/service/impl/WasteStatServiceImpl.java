package com.zkhf.epmis.process.solidWaste.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.NumberUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.solidWaste.WasteStatMapper;
import com.zkhf.epmis.process.solidWaste.domain.*;
import com.zkhf.epmis.process.solidWaste.service.WasteDictService;
import com.zkhf.epmis.process.solidWaste.service.WasteStatService;
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
 * 固废月度统计汇总Service业务层处理
 */
@Slf4j
@Service
public class WasteStatServiceImpl implements WasteStatService {

    private WasteStatMapper wasteStatMapper;
    @Autowired
    public void setWasteStatMapper(WasteStatMapper wasteStatMapper) {
        this.wasteStatMapper = wasteStatMapper;
    }

    private WasteDictService wasteDictService;
    @Autowired
    public void setWasteDictService(WasteDictService wasteDictService) {
        this.wasteDictService = wasteDictService;
    }

    @Override
    public AjaxResult selectStatList(WasteLibReq req) {
        // 返回结果
        List<WasteStatInfo> statList = getStatList(req);
        return AjaxResult.success(statList);
    }

    @Override
    public void exportStat(WasteLibReq req, HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("固废总量统计汇总表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 返回结果
            List<WasteStatInfo> list = getStatList(req);
            if (!list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 从第4行开始插入
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN6 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,6);
                Row row;
                int index = 1;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (WasteStatInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, styleN0);
                    // 废物名称
                    CellUtils.setStringVal(row, cellIndex++, info.getWasteName(), style);
                    // 固废分类
                    CellUtils.setStringVal(row, cellIndex++, info.getWasteCategory(), style);
                    // 设计生产量(t/a)
                    CellUtils.setDoubleVal(row, cellIndex++, info.getDesignOutput(), styleN6);
                    // 实际产生量(t)
                    CellUtils.setDoubleVal(row, cellIndex++, info.getActualGenerateQty(), styleN6);
                    // 减量(t)
                    CellUtils.setDoubleVal(row, cellIndex++, info.getReductionQty(), styleN6);
                    // 入库量(t)
                    CellUtils.setDoubleVal(row, cellIndex++, info.getStorageQty(), styleN6);
                    // 出库量(t)
                    CellUtils.setDoubleVal(row, cellIndex++, info.getOutboundQty(), styleN6);
                    // 当前库存(t)
                    CellUtils.setDoubleVal(row, cellIndex, info.getCurrentInventory(), styleN6);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("固废总量统计汇总表.xlsx", StandardCharsets.UTF_8));
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

    private List<WasteStatInfo> getStatList(WasteLibReq req) {
        // 1. 初始化返回结果
        List<WasteStatInfo> statList = new ArrayList<>();
        // 为空时new对象，便于后边使用
        if (null == req) {
            req = new WasteLibReq();
        }
        // 2. 权限处理
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
            // 非管理员且无企业权限，直接返回空列表
            if (entCodes.isEmpty()) {
                return statList;
            }
        }
        // 3. 查询固废种类
        List<WasteCategory> categoryList = wasteStatMapper.selectCategoryList(entCodes);
        if (null == categoryList || categoryList.isEmpty()) {
            return statList;
        }
        // 转换分类信息-取固废分类根列表
        Map<String, String> dictMap = getWasteDict();
        // 4. 初始化数据结构
        List<String> categoryIds = new ArrayList<>();
        Map<String, WasteStatInfo> statsMap = new HashMap<>();
        // 初始化统计对象
        String perWasteDict = StringUtils.isEmpty(req.getWasteDictId()) ? null : (req.getWasteDictId() + ",");
        for (WasteCategory category : categoryList) {
            String categoryId = category.getCategoryId();
            if (StringUtils.isEmpty(category.getWasteDictId())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getCategoryId()) && !req.getCategoryId().equals(categoryId) ) {
                continue;
            }
            // 分类有没有相同或者开头匹配
            if (null != perWasteDict && !req.getWasteDictId().equals(category.getWasteDictId()) && !category.getWasteDictId().startsWith(perWasteDict)) {
                continue;
            }
            categoryIds.add(categoryId);
            // 补充固废信息
            WasteStatInfo stat = new WasteStatInfo();
            stat.setCategoryId(categoryId);
            stat.setWasteName(category.getWasteName());
            stat.setWasteDictId(category.getWasteDictId());
            if (StringUtils.isNotEmpty(category.getWasteDictId())) {
                String[] ids = category.getWasteDictId().split(",");
                stat.setWasteCategory(dictMap.get(ids[0]));
            }
            stat.setDesignOutput(round(category.getDesignOutput()));
            statsMap.put(categoryId, stat);
            statList.add(stat);
        }
        // 没有命中的直接返回
        if (categoryIds.isEmpty()) {
            return statList;
        }
        // 未过滤减少则全查
        if (categoryIds.size() == categoryList.size()) {
            categoryIds = new ArrayList<>();
        }
        // 5. 查询汇总数据并填充
        List<WasteStatInfo> generateSum = wasteStatMapper.selectGenerateSum(categoryIds);
        for (WasteStatInfo row : generateSum) {
            WasteStatInfo stat = statsMap.get(row.getCategoryId());
            if (stat != null) {
                stat.setActualGenerateQty(row.getActualGenerateQty());
            }
        }
        // 6. 聚合减量记录
        List<WasteStatInfo> reductionSum = wasteStatMapper.selectReductionSum(categoryIds);
        for (WasteStatInfo row : reductionSum) {
            WasteStatInfo stat = statsMap.get(row.getCategoryId());
            if (stat != null) {
                stat.setReductionQty(row.getReductionQty());
            }
        }
        // 7. 聚合入库记录
        List<WasteStatInfo> storageSum = wasteStatMapper.selectStorageSum(categoryIds);
        for (WasteStatInfo row : storageSum) {
            WasteStatInfo stat = statsMap.get(row.getCategoryId());
            if (stat != null) {
                stat.setStorageQty(row.getStorageQty());
            }
        }
        // 8. 聚合出库记录
        List<WasteStatInfo> outboundSum = wasteStatMapper.selectOutboundSum(categoryIds);
        for (WasteStatInfo row : outboundSum) {
            WasteStatInfo stat = statsMap.get(row.getCategoryId());
            if (stat != null) {
                stat.setOutboundQty(row.getOutboundQty());
            }
        }
        // 10. 计算当前库存
        for (WasteStatInfo stat : statList) {
            // 对各统计量进行精度处理（保留 dataScale 位小数，四舍五入）
            // 计算当前库存 = 产生量 - 减量 - 出库量
            double currentInventory = stat.getActualGenerateQty()
                    - stat.getReductionQty()
                    - stat.getOutboundQty();
            // 当前库存也进行精度处理
            stat.setCurrentInventory(round(currentInventory));
            stat.setActualGenerateQty(round(stat.getActualGenerateQty()));
            stat.setReductionQty(round(stat.getReductionQty()));
            stat.setStorageQty(round(stat.getStorageQty()));
            stat.setOutboundQty(round(stat.getOutboundQty()));
        }
        // 11. 排序：按固废ID
        statList.sort(Comparator.comparing(WasteStatInfo::getCurrentInventory).reversed());
        return statList;
    }

    /**
     * 分类信息-取固废分类列表
     */
    private Map<String, String> getWasteDict() {
        Map<String, String> dictMap = new HashMap<>();
        List<WasteDict> dictList = wasteDictService.selectWasteDictList(0L);
        if (null != dictList && !dictList.isEmpty()) {
            dictList.forEach( e -> dictMap.put(e.getId() + "", e.getName()));
        }
        return dictMap;
    }

    /**
     * 精度转换，默认返回0
     */
    private Double round(Double sVal) {
        Double val = NumberUtils.round(sVal, dataScale);
        if (null == val) {
            return 0d;
        }
        return val;
    }
}


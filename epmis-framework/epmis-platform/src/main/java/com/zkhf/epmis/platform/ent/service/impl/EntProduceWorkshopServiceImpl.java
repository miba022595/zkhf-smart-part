package com.zkhf.epmis.platform.ent.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.RelateEnum;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshop;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshopReq;
import com.zkhf.epmis.platform.ent.service.EntProduceWorkshopService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.EntProduceWorkshopMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业生产车间Service业务层处理
 */
@Slf4j
@Service
public class EntProduceWorkshopServiceImpl implements EntProduceWorkshopService {

    private EntProduceWorkshopMapper entProduceWorkshopMapper;
    @Autowired
    public void setEntProduceWorkshopMapper(EntProduceWorkshopMapper entProduceWorkshopMapper) {
        this.entProduceWorkshopMapper = entProduceWorkshopMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectEntProduceWorkshopList(EntProduceWorkshopReq req) {
        if (req == null) {
            req = new EntProduceWorkshopReq();
        }
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EntProduceWorkshop> list = entProduceWorkshopMapper.selectEntProduceWorkshopList(req);
        // 填充关联信息
        fill(list, page, req.getEntCodes());
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "企业生产车间", businessType = BusinessType.EXPORT)
    public void exportEntProduceWorkshop(EntProduceWorkshopReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntProduceWorkshopReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业生产车间列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntProduceWorkshop> list = entProduceWorkshopMapper.selectEntProduceWorkshopList(req);
            if (null != list && !list.isEmpty()) {
                // 填充关联信息
                fill(list, false, req.getEntCodes());
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2; // 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN6 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 6);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntProduceWorkshop info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getEntName());
                    // 生产车间名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getWorkshopName());
                    // 生产车间编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getWorkshopCode());
                    // 归属管理人员
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (StringUtils.isEmpty(info.getPerNickName())) {
                        cell.setCellValue(info.getPerName());
                    } else {
                        cell.setCellValue(info.getPerNickName());
                    }
                    // 归属管理人员
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPerPhone());
                    // 归属管理人员
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPerEmail());
                    // 中心经度
                    cell = CellUtils.getCell(row, cellIndex++, styleN6);
                    if (null != info.getLongitude()) {
                        cell.setCellValue(info.getLongitude());
                    }
                    // 中心纬度
                    cell = CellUtils.getCell(row, cellIndex++, styleN6);
                    if (null != info.getLatitude()) {
                        cell.setCellValue(info.getLatitude());
                    }
                    // 关联生产设施
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getRelateProduceFacilityList() && !info.getRelateProduceFacilityList().isEmpty()) {
                        StringBuilder relateProduceFacility = new StringBuilder();
                        for (Map<String, Object> map : info.getRelateProduceFacilityList()) {
                            if (relateProduceFacility.length() > 0) {
                                relateProduceFacility.append(",");
                            }
                            relateProduceFacility.append(MapUtil.getStr(map, "facilityName"));
                        }
                        cell.setCellValue(relateProduceFacility.toString());
                    }
                    // 关联治理设施
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getRelatePollControlFacilityList() && !info.getRelatePollControlFacilityList().isEmpty()) {
                        StringBuilder relatePollControlFacility = new StringBuilder();
                        for (Map<String, Object> map : info.getRelatePollControlFacilityList()) {
                            if (relatePollControlFacility.length() > 0) {
                                relatePollControlFacility.append(",");
                            }
                            relatePollControlFacility.append(MapUtil.getStr(map, "facilityName"));
                        }
                        cell.setCellValue(relatePollControlFacility.toString());
                    }
                    // 备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业生产车间列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
        }
    }

    private void fill(List<EntProduceWorkshop> list, boolean page, List<String> entCodes) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<String> workshopIds = null;
        if (page) {
            // 提取车间ID列表
            workshopIds = list.stream().map(EntProduceWorkshop::getWorkshopId).collect(Collectors.toList());
        }
        // 批量查询关联设施信息
        List<Map<String, Object>> relateList = entProduceWorkshopMapper.selectEntProduceWorkshopRelateList(RelateEnum.produceWorkshop.name(), entCodes, workshopIds);
        if (null == relateList || relateList.isEmpty()) {
            return;
        }
        // 依关联类型区分，produceFacility 生产设施，pollControlFacility治理设施, workshopId, facilityType, facilityId, facilityName
        Map<String, List<Map<String, Object>>> produceFacility = new HashMap<>();
        Map<String, List<Map<String, Object>>> pollControlFacility = new HashMap<>();
        relateList.forEach(map -> {
            String workshopId = map.remove("workshopId") + "";
            String facilityType = map.remove("facilityType") + "";
            if ("produceFacility".equals(facilityType)) {
                if (!produceFacility.containsKey(workshopId)) {
                    produceFacility.put(workshopId, new ArrayList<>());
                }
                produceFacility.get(workshopId).add(map);
            } else if ("pollControlFacility".equals(facilityType)) {
                if (!pollControlFacility.containsKey(workshopId)) {
                    pollControlFacility.put(workshopId, new ArrayList<>());
                }
                pollControlFacility.get(workshopId).add(map);
            }
        });
        list.forEach( e -> {
            if (produceFacility.containsKey(e.getWorkshopId())) {
                e.setRelateProduceFacilityList(produceFacility.get(e.getWorkshopId()));
            } else {
                e.setRelateProduceFacilityList(new ArrayList<>());
            }
            if (pollControlFacility.containsKey(e.getWorkshopId())) {
                e.setRelatePollControlFacilityList(pollControlFacility.get(e.getWorkshopId()));
            } else {
                e.setRelatePollControlFacilityList(new ArrayList<>());
            }
        });
    }

    @Override
    @Log(title = "企业生产车间", businessType = BusinessType.INSERT)
    public AjaxResult insertEntProduceWorkshop(EntProduceWorkshop info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setWorkshopId(UlidCreator.getMonotonicUlid().toString());
        int result = entProduceWorkshopMapper.insertEntProduceWorkshop(info);
        if (result > 0) {
            // 更新车间关关系
            updateRelateInfo(info.getWorkshopId(), info.getProduceFacilityIds(), info.getPollControlFacilityIds());
            // 设置附件
            if (info.getAnnexIds() != null && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getWorkshopId(), AnnexTypeEnum.EntProduceWorkshop.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业生产车间", businessType = BusinessType.UPDATE)
    public AjaxResult updateEntProduceWorkshop(EntProduceWorkshop info) {
        int count = entProduceWorkshopMapper.updateEntProduceWorkshop(info);
        if (count > 0) {
            // 更新车间关关系
            updateRelateInfo(info.getWorkshopId(), info.getProduceFacilityIds(), info.getPollControlFacilityIds());
            // 更新附件
            annexService.updateAnnex(info.getWorkshopId(), AnnexTypeEnum.EntProduceWorkshop.name(), info.getAnnexIds());
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业生产车间", businessType = BusinessType.DELETE)
    public AjaxResult deleteEntProduceWorkshopById(String workshopId) {
        if (StringUtils.isEmpty(workshopId)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = entProduceWorkshopMapper.deleteEntProduceWorkshopById(workshopId);
        if (count > 0) {
            // 更新车间关关系
            updateRelateInfo(workshopId, null, null);
            // 删除项目附件
            annexService.updateAnnex(workshopId, AnnexTypeEnum.EntProduceWorkshop.name(), null);
        }
        return AjaxResult.success(count);
    }

    private void updateRelateInfo(String workshopId, List<String> produceFacilityIds, List<String> pollControlFacilityIds) {
        // 删除旧的关联生产设施关系
        entProduceWorkshopMapper.deleteRelateProduceFacilityById(RelateEnum.produceWorkshop.name(), workshopId);
        // 删除旧的关联污染治理设施关系
        entProduceWorkshopMapper.deleteRelatePollControlFacilityById(RelateEnum.produceWorkshop.name(), workshopId);
        // 添加新的关联生产设施关系
        if (null != produceFacilityIds && !produceFacilityIds.isEmpty()) {
            entProduceWorkshopMapper.insertRelateProduceFacility(RelateEnum.produceWorkshop.name(), workshopId, produceFacilityIds);
        }
        // 添加新的关联污染治理设施关系
        if (null != pollControlFacilityIds && !pollControlFacilityIds.isEmpty()) {
            entProduceWorkshopMapper.insertRelatePollControlFacility(RelateEnum.produceWorkshop.name(), workshopId, pollControlFacilityIds);
        }
    }
}


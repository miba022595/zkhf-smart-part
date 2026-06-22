package com.zkhf.epmis.platform.ent.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.PublicStatusEnum;
import com.zkhf.epmis.core.enums.RalateTypeEnum;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.ent.domain.EntProductionLine;
import com.zkhf.epmis.platform.ent.domain.EntProductionLineReq;
import com.zkhf.epmis.platform.ent.service.EntProductionLineService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.BasRelateMapper;
import com.zkhf.epmis.platform.mapper.ent.EntProductionLineMapper;
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
import java.util.*;

/**
 * 企业生产线信息Service业务层处理
 */
@Slf4j
@Service
public class EntProductionLineServiceImpl implements EntProductionLineService {

    private EntProductionLineMapper entProductionLineMapper;
    @Autowired
    public void setEntProductionLineMapper(EntProductionLineMapper entProductionLineMapper) {
        this.entProductionLineMapper = entProductionLineMapper;
    }

    private BasRelateMapper basRelateMapper;
    @Autowired
    public void setBasRelateMapper(BasRelateMapper basRelateMapper) {
        this.basRelateMapper = basRelateMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectEntProductionLineDetailByLineId(String lineId) {
        EntProductionLine line = entProductionLineMapper.selectEntProductionLineByLineId(lineId);
        if (null != line) {
            List<Map<String, Object>> relateList = entProductionLineMapper.selectEntProductionLineRelateList(Collections.singletonList(lineId),
                    RalateTypeEnum.line_production_facility.name(),
                    RalateTypeEnum.line_governance_facility.name(),
                    RalateTypeEnum.line_output.name());
            if (null != relateList && !relateList.isEmpty()) {
                for (Map<String, Object> relateMap : relateList) {
                    String sLineId = MapUtil.getStr(relateMap, "lineId");
                    if (!lineId.equals(sLineId)) {
                        continue;
                    }
                    // 设置关联信息
                    fill(relateMap, line);
                }
            }
            // 生产设施用电附件
            line.setProductionFacilityAnnexList(annexService.selectAnnexList(lineId, AnnexTypeEnum.entLineProductionFacility.name()));
            // 治理设施用电图附件
            line.setGovernanceFacilityAnnexList(annexService.selectAnnexList(lineId, AnnexTypeEnum.entLineGovernanceFacility.name()));
        }
        return AjaxResult.success(line);
    }

    @Override
    public AjaxResult selectEntProductionLineList(EntProductionLineReq req) {
        if (null == req) {
            req = new EntProductionLineReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EntProductionLine> list = entProductionLineMapper.selectEntProductionLineList(req);
        // 填充关联的生产设施、治理设施、排口的信息
        fill(list, page);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "企业生产线信息", businessType = BusinessType.EXPORT)
    public void exportEntProductionLine(EntProductionLineReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntProductionLineReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业生产线列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntProductionLine> list = entProductionLineMapper.selectEntProductionLineList(req);
            if (null != list && !list.isEmpty()) {
                // 填充关联信息
                fill(list, false);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2; // 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntProductionLine line : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getEntName());
                    // 企业编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getEntCode());
                    // 生产车间名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getWorkshopName());
                    // 生产车间编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getWorkshopCode());
                    // 生产线名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getLineName());
                    // 生产线编码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getLineCode());
                    // 工艺类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getProcessType());
                    // 产品类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(line.getProductType());
                    // 状态
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(PublicStatusEnum.getNameByCode(line.getStatus()));
                    // 设计产能
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != line.getCapacity()) {
                        String value = line.getCapacity().toString();
                        if (StringUtils.isNotEmpty(line.getCapacityUnit())) {
                            value = value + " " + line.getCapacityUnit();
                        }
                        cell.setCellValue(value);
                    }
                    // 关联生产设施
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != line.getProduceFacilityList() && !line.getProduceFacilityList().isEmpty()) {
                        StringBuilder relateProduceFacility = new StringBuilder();
                        for (Map<String, Object> map : line.getProduceFacilityList()) {
                            if (relateProduceFacility.length() > 0) {
                                relateProduceFacility.append(",");
                            }
                            relateProduceFacility.append(MapUtil.getStr(map, "name"));
                        }
                        cell.setCellValue(relateProduceFacility.toString());
                    }
                    // 关联治理设施
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != line.getGovernanceFacilityList() && !line.getGovernanceFacilityList().isEmpty()) {
                        StringBuilder governanceFacility = new StringBuilder();
                        for (Map<String, Object> map : line.getGovernanceFacilityList()) {
                            if (governanceFacility.length() > 0) {
                                governanceFacility.append(",");
                            }
                            governanceFacility.append(MapUtil.getStr(map, "name"));
                        }
                        cell.setCellValue(governanceFacility.toString());
                    }
                    // 关联排口
                    cell = CellUtils.getCell(row, cellIndex, style);
                    if (null != line.getOutPutList() && !line.getOutPutList().isEmpty()) {
                        StringBuilder outPut = new StringBuilder();
                        for (Map<String, Object> map : line.getOutPutList()) {
                            if (outPut.length() > 0) {
                                outPut.append(",");
                            }
                            outPut.append(MapUtil.getStr(map, "name"));
                        }
                        cell.setCellValue(outPut.toString());
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业生产线列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<EntProductionLine> list, boolean page) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<String> ids = null;
        Map<String, EntProductionLine> map = new HashMap<>();
        if (page) {
            ids = new ArrayList<>();
        }
        for (EntProductionLine line : list) {
            if (null != ids) {
                ids.add(line.getLineId());
            }
            map.put(line.getLineId(), line);
        }
        List<Map<String, Object>> relateList = entProductionLineMapper.selectEntProductionLineRelateList(ids,
                RalateTypeEnum.line_production_facility.name(),
                RalateTypeEnum.line_governance_facility.name(),
                RalateTypeEnum.line_output.name());
        if (null == relateList || relateList.isEmpty()) {
            return;
        }
        for (Map<String, Object> relateMap : relateList) {
            EntProductionLine line = map.get(MapUtil.getStr(relateMap, "lineId"));
            if (null == line) {
                continue;
            }
            // 设置关联信息
            fill(relateMap, line);
        }
    }

    private void fill(Map<String, Object> map, EntProductionLine line) {
        String dataType = MapUtil.getStr(map, "dataType");
        Map<String, Object> target = new HashMap<>();
        target.put("id", map.get("id"));
        target.put("code", map.get("code"));
        target.put("name", map.get("name"));
        if (RalateTypeEnum.line_production_facility.name().equals(dataType)) {
            if (null == line.getProduceFacilityList()) {
                line.setProduceFacilityList(new ArrayList<>());
            }
            line.getProduceFacilityList().add(target);
        } else if (RalateTypeEnum.line_governance_facility.name().equals(dataType)) {
            if (null == line.getGovernanceFacilityList()) {
                line.setGovernanceFacilityList(new ArrayList<>());
            }
            line.getGovernanceFacilityList().add(target);
        } else if (RalateTypeEnum.line_output.name().equals(dataType)) {
            target.put("type", map.get("type"));
            if (null == line.getOutPutList()) {
                line.setOutPutList(new ArrayList<>());
            }
            line.getOutPutList().add(target);
        }
    }

    @Override
    @Log(title = "企业生产线信息", businessType = BusinessType.INSERT)
    public AjaxResult insertEntProductionLine(EntProductionLine line) {
        if (StringUtils.isEmpty(line.getWorkshopId())) {
            return AjaxResult.error("产线未选择归属车间");
        }
        if (StringUtils.isEmpty(line.getLineCode())) {
            return AjaxResult.error("生产线编码不能为空");
        }
        // 判断生产线编码是否重复
        String oldLineId = entProductionLineMapper.selectLineIdByWorkLineCode(line.getWorkshopId(), line.getLineCode());
        if (StringUtils.isNotEmpty(oldLineId)) {
            return AjaxResult.error("车间已存在相同的产线编码");
        }
        line.setLineId(UlidCreator.getMonotonicUlid().toString());
        int count = entProductionLineMapper.insertEntProductionLine(line);
        if (count > 0) {
            // 更新产线关联关系
            updateRelateInfo(line.getLineId(), line.getProduceFacilityIds(), line.getGovernanceFacilityIds(), line.getOutPutIdList());
            // 生产设施用电附件
            if (line.getProductionFacilityAnnexIds() != null && !line.getProductionFacilityAnnexIds().isEmpty()) {
                annexService.updateAnnex(line.getLineId(), AnnexTypeEnum.entLineProductionFacility.name(), line.getProductionFacilityAnnexIds());
            }
            // 治理设施用电图附件
            if (line.getGovernanceFacilityAnnexIds() != null && !line.getGovernanceFacilityAnnexIds().isEmpty()) {
                annexService.updateAnnex(line.getLineId(), AnnexTypeEnum.entLineGovernanceFacility.name(), line.getGovernanceFacilityAnnexIds());
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "企业生产线信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateEntProductionLine(EntProductionLine line) {
        // 修改时编码可以不修改
        if (StringUtils.isNotEmpty(line.getLineCode())) {
            // 判断生产线编码是否重复
            String oldLineId = entProductionLineMapper.selectLineIdByLineIdCode(line.getLineId(), line.getLineCode());
            if (StringUtils.isNotEmpty(oldLineId) && !oldLineId.equals(line.getLineId())) {
                return AjaxResult.error("车间下产线编码重复");
            }
        }
        int count = entProductionLineMapper.updateEntProductionLine(line);
        if (count > 0) {
            // 更新产线关联关系
            updateRelateInfo(line.getLineId(), line.getProduceFacilityIds(), line.getGovernanceFacilityIds(), line.getOutPutIdList());
            // 生产设施用电附件
            annexService.updateAnnex(line.getLineId(), AnnexTypeEnum.entLineProductionFacility.name(), line.getProductionFacilityAnnexIds());
            // 治理设施用电图附件
            annexService.updateAnnex(line.getLineId(), AnnexTypeEnum.entLineGovernanceFacility.name(), line.getGovernanceFacilityAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "企业生产线信息", businessType = BusinessType.DELETE)
    public AjaxResult deleteEntProductionLineById(String lineId) {
        if (StringUtils.isEmpty(lineId)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = entProductionLineMapper.deleteEntProductionLineById(lineId);
        if (count > 0) {
            // 更新产线关联关系
            updateRelateInfo(lineId, null, null, null);
            // 生产设施用电附件
            annexService.updateAnnex(lineId, AnnexTypeEnum.entLineProductionFacility.name(), null);
            // 治理设施用电图附件
            annexService.updateAnnex(lineId, AnnexTypeEnum.entLineGovernanceFacility.name(), null);
        }
        return AjaxResult.success(count);
    }

    private void updateRelateInfo(String lineId, List<String> produceFacilityIds, List<String> governanceFacilityIds, List<String> outPutIdList) {
        // 删除旧的关联关系
        basRelateMapper.deleteOldRelateBySourTarType(lineId, Arrays.asList(RalateTypeEnum.line_production_facility.name(),
                RalateTypeEnum.line_governance_facility.name(), RalateTypeEnum.line_output.name()));
        // 关联生产设施
        List<Map<String, Object>> relateList = new ArrayList<>();
        if (null != produceFacilityIds && !produceFacilityIds.isEmpty()) {
            produceFacilityIds.forEach( e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("targetType", RalateTypeEnum.line_production_facility.name());
                map.put("targetId", e);
                relateList.add(map);
            });
        }
        // 关联污染治理设施
        if (null != governanceFacilityIds && !governanceFacilityIds.isEmpty()) {
            governanceFacilityIds.forEach( e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("targetType", RalateTypeEnum.line_governance_facility.name());
                map.put("targetId", e);
                relateList.add(map);
            });
        }
        // 关联排口
        if (null != outPutIdList && !outPutIdList.isEmpty()) {
            outPutIdList.forEach( e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("targetType", RalateTypeEnum.line_output.name());
                map.put("targetId", e);
                relateList.add(map);
            });
        }
        // 添加新的关联关系
        if (!relateList.isEmpty()) {
            basRelateMapper.insertRelateProduceFacility(lineId, relateList);
        }
    }
}


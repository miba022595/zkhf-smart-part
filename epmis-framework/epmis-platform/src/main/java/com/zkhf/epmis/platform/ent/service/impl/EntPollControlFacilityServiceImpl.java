package com.zkhf.epmis.platform.ent.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.enums.RelateEnum;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacility;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacilityReq;
import com.zkhf.epmis.platform.ent.service.EntPollControlFacilityService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.EntPollControlFacilityMapper;
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

/**
 * 企业污染治理设施Service业务层处理
 */
@Slf4j
@Service
public class EntPollControlFacilityServiceImpl implements EntPollControlFacilityService {

    private EntPollControlFacilityMapper entPollControlFacilityMapper;
    @Autowired
    public void setEntPollControlFacilityMapper(EntPollControlFacilityMapper entPollControlFacilityMapper) {
        this.entPollControlFacilityMapper = entPollControlFacilityMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectEntPollControlFacilityList(EntPollControlFacilityReq req) {
        if (req == null) {
            req = new EntPollControlFacilityReq();
        }
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EntPollControlFacility> list = entPollControlFacilityMapper.selectEntPollControlFacilityList(req);
        AjaxResult result = PageUtils.getAjaxResult(list, page);
        if (page && !list.isEmpty()) { // 分页时填充关联的排口信息
            Map<String, EntPollControlFacility> map = new HashMap<>();
            list.forEach( e -> {
                e.setRelateOutPutList(new ArrayList<>());
                map.put(e.getFacilityId(), e);
            });
            List<Map<String, Object>> relateList = entPollControlFacilityMapper.selectRelateOutPutList(RelateEnum.outPut.name(), new ArrayList<>(map.keySet()));
            relateList.forEach( e -> {
                String facilityId = e.remove("facilityId") + "";
                if (map.containsKey(facilityId)) {
                    map.get(facilityId).getRelateOutPutList().add(e);
                }
            });
        }
        return result;
    }

    @Override
    @Log(title = "企业污染治理设施", businessType = BusinessType.EXPORT)
    public void exportEntPollControlFacility(EntPollControlFacilityReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntPollControlFacilityReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业污染治理设施列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntPollControlFacility> list = entPollControlFacilityMapper.selectEntPollControlFacilityList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2; // 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntPollControlFacility info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getEntName());
                    // 排放口类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(OutPutTypeEnum.getNameByCode(info.getOutPutType()));
                    // 治理设施编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityCode());
                    // 治理设施名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityName());
                    // 主要污染物
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutant());
                    // 治理工艺
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getGovernProcess());
                    // 设计治理效率(0-100%)
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getEfficiency()) {
                        cell.setCellValue(info.getEfficiency());
                    }
                    // 设计处理能力
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getDesignCapacity());
                    // 设计治理设施运行率(0-100%)
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getActualOperatingRate()) {
                        cell.setCellValue(info.getActualOperatingRate());
                    }
                    // 安装时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getInstallDate()) {
                        cell.setCellValue(info.getInstallDate().format(DateUtils.yy_m_d));
                    }
                    // 备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业污染治理设施列表.xlsx", StandardCharsets.UTF_8));
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

    @Override
    @Log(title = "企业污染治理设施", businessType = BusinessType.INSERT)
    public AjaxResult insertEntPollControlFacility(EntPollControlFacility info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setFacilityId(UlidCreator.getMonotonicUlid().toString());
        int count = entPollControlFacilityMapper.insertEntPollControlFacility(info);
        if (count > 0) {
            // 更新附件
            if (null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getFacilityId(), AnnexTypeEnum.entPollControlFacility.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业污染治理设施", businessType = BusinessType.UPDATE)
    public AjaxResult updateEntPollControlFacility(EntPollControlFacility info) {
        int count = entPollControlFacilityMapper.updateEntPollControlFacility(info);
        if (count > 0) {
            // 更新附件
            annexService.updateAnnex(info.getFacilityId(), AnnexTypeEnum.entPollControlFacility.name(), info.getAnnexIds());
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业污染治理设施", businessType = BusinessType.DELETE)
    public AjaxResult deleteEntPollControlFacilityById(String id) {
        if (StringUtils.isEmpty(id)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = entPollControlFacilityMapper.deleteEntPollControlFacilityById(id);
        if (count > 0) {
            // 删除关联关系
            entPollControlFacilityMapper.deleteEntPollControlFacilityRelateById(id);
            // 删除项目附件
            annexService.updateAnnex(id, AnnexTypeEnum.entPollControlFacility.name(), null);
        }
        return AjaxResult.success(count);
    }
}


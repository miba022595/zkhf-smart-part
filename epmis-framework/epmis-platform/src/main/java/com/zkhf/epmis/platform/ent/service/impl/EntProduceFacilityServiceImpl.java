package com.zkhf.epmis.platform.ent.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacility;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacilityReq;
import com.zkhf.epmis.platform.ent.service.EntProduceFacilityService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.EntProduceFacilityMapper;
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
import java.util.List;

/**
 * 企业生产设施/设备Service业务层处理
 */
@Slf4j
@Service
public class EntProduceFacilityServiceImpl implements EntProduceFacilityService {

    private EntProduceFacilityMapper entProduceFacilityMapper;
    @Autowired
    public void setEntProduceFacilityMapper(EntProduceFacilityMapper entProduceFacilityMapper) {
        this.entProduceFacilityMapper = entProduceFacilityMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectEntProduceFacilityList(EntProduceFacilityReq req) {
        if (req == null) {
            req = new EntProduceFacilityReq();
        }
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EntProduceFacility> list = entProduceFacilityMapper.selectEntProduceFacilityList(req);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "企业生产设施/设备", businessType = BusinessType.EXPORT)
    public void exportEntProduceFacility(EntProduceFacilityReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntProduceFacilityReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业生产设施列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntProduceFacility> list = entProduceFacilityMapper.selectEntProduceFacilityList(req);
            // 获取单元格格式
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2; // 首行

                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle numStyle0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntProduceFacility info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getEntName());
                    // 生产设施名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityName());
                    // 生产设施编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityCode());
                    // 设备类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityType());
                    // 设备规格
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getSpecification());
                    // 型号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityModel());
                    // 制造商/供应商
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getSupplier());
                    // 购置时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getBuyDate()) {
                        cell.setCellValue(info.getBuyDate().format(DateUtils.yy_m_d));
                    }
                    // 设备状态：1-在用，2-停用
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getEquipmentStatus()) {
                        if (EntProduceFacility.equipmentStatus_ON.equals(info.getEquipmentStatus())) {
                            cell.setCellValue("在用");
                        } else if (EntProduceFacility.equipmentStatus_OFF.equals(info.getEquipmentStatus())) {
                            cell.setCellValue("停用");
                        }
                    }
                    // 设施数量
                    cell = CellUtils.getCell(row, cellIndex++, numStyle0);
                    if (null != info.getFacilityNumber()) {
                        cell.setCellValue(info.getFacilityNumber());
                    }
                    // 计量单位
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMeasureUnit());
                    // 设计生产能力
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getDesignCapacity());
                    // 备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业生产设施列表.xlsx", StandardCharsets.UTF_8));
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
    @Log(title = "企业生产设施/设备", businessType = BusinessType.INSERT)
    public AjaxResult insertEntProduceFacility(EntProduceFacility info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setFacilityId(UlidCreator.getMonotonicUlid().toString());
        int result = entProduceFacilityMapper.insertEntProduceFacility(info);
        if (result > 0) {
            // 设置附件
            if (info.getAnnexIds() != null && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getFacilityId(), AnnexTypeEnum.EntProduceFacility.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业生产设施/设备", businessType = BusinessType.UPDATE)
    public AjaxResult updateEntProduceFacility(EntProduceFacility info) {
        int count = entProduceFacilityMapper.updateEntProduceFacility(info);
        if (count > 0) {
            // 更新附件
            annexService.updateAnnex(info.getFacilityId(), AnnexTypeEnum.EntProduceFacility.name(), info.getAnnexIds());
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业生产设施/设备", businessType = BusinessType.DELETE)
    public AjaxResult deleteEntProduceFacilityById(String facilityId) {
        if (StringUtils.isEmpty(facilityId)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = entProduceFacilityMapper.deleteEntProduceFacilityById(facilityId);
        if (count > 0) {
            // 删除关联关系
            entProduceFacilityMapper.deleteEntProduceFacilityRelateById(facilityId);
            // 删除项目附件
            annexService.updateAnnex(facilityId, AnnexTypeEnum.EntProduceFacility.name(), null);
        }
        return AjaxResult.success(count);
    }
}


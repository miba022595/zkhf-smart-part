package com.zkhf.epmis.platform.envProtect.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envProtect.domain.EntCleanProduce;
import com.zkhf.epmis.platform.envProtect.domain.EntCleanProduceReq;
import com.zkhf.epmis.platform.envProtect.service.EntCleanProduceService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.EntCleanProduceMapper;
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
 * 企业清洁生产基础Service业务层处理
 */
@Slf4j
@Service
public class EntCleanProduceServiceImpl implements EntCleanProduceService {

    private EntCleanProduceMapper entCleanProduceMapper;

    @Autowired
    public void setEntCleanProduceMapper(EntCleanProduceMapper entCleanProduceMapper) {
        this.entCleanProduceMapper = entCleanProduceMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectCleanProduceById(String cleanProduceId) {
        EntCleanProduce info = entCleanProduceMapper.selectCleanProduceById(cleanProduceId);
        if (null == info) {
            return AjaxResult.error("清洁生产信息为空");
        }
        info.setAnnexInfoList(annexService.selectAnnexList(cleanProduceId, AnnexTypeEnum.entCleanProduce.name()));
        return AjaxResult.success(info);
    }

    @Override
    public AjaxResult selectCleanProduceList(EntCleanProduceReq req) {
        if (null == req) {
            req = new EntCleanProduceReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EntCleanProduce> list = entCleanProduceMapper.selectCleanProduceList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "企业清洁生产基础", businessType = BusinessType.EXPORT)
    public void exportCleanProduce(EntCleanProduceReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntCleanProduceReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("清洁生产列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntCleanProduce> list = entCleanProduceMapper.selectCleanProduceList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntCleanProduce produce : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(produce.getCleanName());
                    // 编制单位
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(produce.getEntName());
                    // 编制时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(null == produce.getMakeDate() ? "" : produce.getMakeDate().toString());
                    // 审核重点
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(produce.getAuditFocus());
                    // 方案情况
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(produce.getPlanInfo());
                    // 减排效果
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(produce.getReduceEffect());
                    // 工作进展
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(produce.getWorkProgress());
                    // 实施时间
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(null == produce.getEffectiveDate() ? "" : produce.getEffectiveDate().toString());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业清洁生产列表.xlsx", StandardCharsets.UTF_8));
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

    @Override
    @Log(title = "企业清洁生产基础", businessType = BusinessType.INSERT)
    public AjaxResult insertCleanProduce(EntCleanProduce produce) {
        if (StringUtils.isEmpty(produce.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        produce.setCleanProduceId(UlidCreator.getMonotonicUlid().toString());
        int count = entCleanProduceMapper.insertCleanProduce(produce);
        if (count > 0 && null != produce.getAnnexIds() && !produce.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(produce.getCleanProduceId(), AnnexTypeEnum.entCleanProduce.name(), produce.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "企业清洁生产基础", businessType = BusinessType.UPDATE)
    public AjaxResult updateCleanProduce(EntCleanProduce produce) {
        int count = entCleanProduceMapper.updateCleanProduce(produce);
        if (count > 0) {
            annexService.updateAnnex(produce.getCleanProduceId(), AnnexTypeEnum.entCleanProduce.name(), produce.getAnnexIds());
        }
        return AjaxResult.success();
    }

    @Override
    @Log(title = "企业清洁生产基础", businessType = BusinessType.DELETE)
    public AjaxResult deleteCleanProduceByIds(List<String> ids) {
        if (null == ids || ids.isEmpty()) {
            return AjaxResult.error("请求信息为空");
        }
        int count = entCleanProduceMapper.deleteCleanProduceByIds(ids);
        if (count > 0) {
            // 删除附件
            ids.forEach(e -> annexService.updateAnnex(e, AnnexTypeEnum.entCleanProduce.name(), null));
        }
        return AjaxResult.success(count);
    }
}


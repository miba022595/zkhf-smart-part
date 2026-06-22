package com.zkhf.epmis.platform.envFee.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.enums.InvoiceType;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoice;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoiceReq;
import com.zkhf.epmis.platform.envFee.service.EnvFeeInvoiceService;
import com.zkhf.epmis.platform.envFee.service.EnvFeeService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envFee.EnvFeeInvoiceMapper;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 费用发票信息Service业务层处理
 */
@Slf4j
@Service
public class EnvFeeInvoiceServiceImpl implements EnvFeeInvoiceService {

    private EnvFeeInvoiceMapper envFeeInvoiceMapper;
    @Autowired
    public void setEnvFeeInvoicesMapper(EnvFeeInvoiceMapper envFeeInvoiceMapper) {
        this.envFeeInvoiceMapper = envFeeInvoiceMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private EnvFeeService envFeeService;
    @Autowired
    public void setEnvFeesService(EnvFeeService envFeeService) {
        this.envFeeService = envFeeService;
    }

    @Override
    public AjaxResult selectEnvFeeInvoiceListByFeeId(String feeId) {
        List<EnvFeeInvoice> list = envFeeInvoiceMapper.selectEnvFeeInvoiceListByFeeId(feeId);
        // 填充数据
        fill(list);
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult selectEnvFeeInvoiceList(EnvFeeInvoiceReq req) {
        if (null == req) {
            req = new EnvFeeInvoiceReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EnvFeeInvoice> list = envFeeInvoiceMapper.selectEnvFeeInvoiceList(req);
        // 填充数据
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "费用发票信息", businessType = BusinessType.EXPORT)
    public void exportEnvFeeInvoice(EnvFeeInvoiceReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvFeeInvoiceReq();
        }
        OutputStream outputStream = null;
        try {
            String templatePath = "template/环保费用发票列表模板.xlsx";
            InputStream fis = getClass().getClassLoader().getResourceAsStream(templatePath);
            if (fis == null) {
                log.error("无法从路径加载资源 {}", templatePath);
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvFeeInvoice> list = envFeeInvoiceMapper.selectEnvFeeInvoiceList(req);
            // 填充数据
            fill(list);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 2;// 首行

            CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
            CellStyle numStyle0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
            CellStyle numStyle2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
            if (null != list && !list.isEmpty()) {
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvFeeInvoice invoice : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 发票号码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(invoice.getInvoiceNumber());
                    // 发票代码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(invoice.getInvoiceCode());
                    // 发票金额（不含税）
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != invoice.getInvoiceAmount()) {
                        cell.setCellValue(invoice.getInvoiceAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 税额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != invoice.getTaxAmount()) {
                        cell.setCellValue(invoice.getTaxAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 发票价税合计金额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != invoice.getTotalAmount()) {
                        cell.setCellValue(invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 开票日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != invoice.getInvoiceDate()) {
                        cell.setCellValue(invoice.getInvoiceDate().format(DateUtils.yy_m_d));
                    }
                    // 税率（单位：百分比，如13表示13%）
                    cell = CellUtils.getCell(row, cellIndex++, numStyle0);
                    if (null != invoice.getTaxRate()) {
                        cell.setCellValue(invoice.getTaxRate());
                    }
                    // 发票类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(invoice.getInvoiceTypeDesc());
                    // 销售方名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(invoice.getSellerName());
                    // 销售方税号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(invoice.getSellerTaxId());
                    // 发票状态：1-正常 2-已红冲
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (EnvFeeInvoice.INVOICE_NORMAL.equals(invoice.getInvoiceStatus())) {
                        cell.setCellValue("正常");
                    } else if (EnvFeeInvoice.INVOICE_RED_WASH.equals(invoice.getInvoiceStatus())) {
                        cell.setCellValue("已红冲");
                    }
                    // 发票校验码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(invoice.getCheckCode());
                    // 发票备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(invoice.getRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用发票列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<EnvFeeInvoice> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        list.forEach( e -> {
            e.setInvoiceTypeDesc(InvoiceType.getNameByCode(e.getInvoiceType()));
            if (null != e.getInvoiceAmount() && null != e.getTaxAmount()) {
                e.setTotalAmount(e.getInvoiceAmount().add(e.getTaxAmount()));
            }
        });
    }

    @Override
    @Log(title = "费用发票信息", businessType = BusinessType.INSERT)
    public AjaxResult insertEnvFeeInvoice(EnvFeeInvoice envFeeInvoice) {
        if (StringUtils.isEmpty(envFeeInvoice.getInvoiceNumber())) {
            return AjaxResult.error("发票号码不能为空");
        }
        // 校验发票号码是否已存在
        int count = envFeeInvoiceMapper.checkExistsInvoiceNumber(envFeeInvoice.getInvoiceNumber());
        if (count > 0) {
            return AjaxResult.error("发票号码已存在，请确认");
        }
        count = envFeeInvoiceMapper.insertEnvFeeInvoice(envFeeInvoice);
        if (count > 0 && null != envFeeInvoice.getAnnexIds() && !envFeeInvoice.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(envFeeInvoice.getInvoiceNumber(), AnnexTypeEnum.envFeeInvoice.name(), envFeeInvoice.getAnnexIds());
        }
        if (count > 0) {
            // 费用登记中的开票金额更新
            envFeeService.updateEnvFeeInvoiceAmount(envFeeInvoice.getFeeId());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "费用发票信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvFeeInvoice(EnvFeeInvoice envFeeInvoice) {
        if (StringUtils.isEmpty(envFeeInvoice.getInvoiceNumber())) {
            return AjaxResult.error("发票号码不能为空");
        }
        int count = envFeeInvoiceMapper.updateEnvFeeInvoice(envFeeInvoice);
        if (count > 0) {
            annexService.updateAnnex(envFeeInvoice.getInvoiceNumber(), AnnexTypeEnum.envFeeInvoice.name(), envFeeInvoice.getAnnexIds());
            // 费用登记中的开票金额更新
            envFeeService.updateEnvFeeInvoiceAmount(envFeeInvoice.getFeeId());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "费用发票信息", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnvFeeInvoiceById(String invoiceNumber) {
        EnvFeeInvoice info = envFeeInvoiceMapper.selectEnvFeeInvoiceById(invoiceNumber);
        if (null == info) {
            return AjaxResult.error("费用发票信息不存在");
        }
        int count = envFeeInvoiceMapper.deleteEnvFeeInvoiceById(invoiceNumber);
        if (count > 0) {
            // 删除附件
            annexService.updateAnnex(invoiceNumber, AnnexTypeEnum.envFeeInvoice.name(), null);
            // 费用登记中的开票金额更新
            envFeeService.updateEnvFeeInvoiceAmount(info.getFeeId());
        }
        return AjaxResult.success(count);
    }
}


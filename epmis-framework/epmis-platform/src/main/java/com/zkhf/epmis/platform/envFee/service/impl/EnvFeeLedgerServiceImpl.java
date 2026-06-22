package com.zkhf.epmis.platform.envFee.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.envFee.dict.FeeTypeDict;
import com.zkhf.epmis.platform.envFee.domain.*;
import com.zkhf.epmis.platform.envFee.service.EnvFeeDictService;
import com.zkhf.epmis.platform.envFee.service.EnvFeeLedgerService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envFee.EnvFeeLedgerMapper;
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
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环保费用台账Service业务层处理
 */
@Slf4j
@Service
public class EnvFeeLedgerServiceImpl implements EnvFeeLedgerService {

    private EnvFeeLedgerMapper envFeeLedgerMapper;
    @Autowired
    public void setEnvFeeLedgerMapper(EnvFeeLedgerMapper envFeeLedgerMapper) {
        this.envFeeLedgerMapper = envFeeLedgerMapper;
    }

    private EnvFeeDictService envFeeDictService;
    @Autowired
    public void setEnvFeeDictService(EnvFeeDictService envFeeDictService) {
        this.envFeeDictService = envFeeDictService;
    }

    @Override
    public AjaxResult selectEnvFeeLedgerList(EnvFeeLedgerReq req) {
        // 请求参数校验
        req = initReq(req);
        // 过滤的状态列表
        req.setStatusList(Arrays.asList("APPROVED", "PARTIAL_PAID", "INVOICED", "PAID", "COMPLETED", "ARCHIVED", "OVERDUE"));
        // 分页查询
        PageUtils.startPage();
        List<Ledger> list = envFeeLedgerMapper.selectEnvFeeLedgerList(req);
        // 填充数据
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void exportEnvFeeLedger(EnvFeeLedgerReq req, HttpServletResponse response) {
        // 请求参数校验
        req = initReq(req);
        // 过滤的状态列表
        req.setStatusList(Arrays.asList("APPROVED", "PARTIAL_PAID", "INVOICED", "PAID", "COMPLETED", "ARCHIVED", "OVERDUE"));
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保费用台账列表模板.xlsx");
            if (null == workbook) {
                return;
            }
            List<Ledger> list = envFeeLedgerMapper.selectEnvFeeLedgerList(req);
            if (null != list && !list.isEmpty()) {
                // 填充数据
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 4;// 首行
                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (Ledger ledger : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 费用类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ledger.getFeeTypeName());
                    // 年份
                    cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    cell.setCellValue(ledger.getYearNum());
                    // 总费用
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, ledger.getYearTotalFee());
                    // 总付款金额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, ledger.getYearTotalInvoice());
                    // 总开票金额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, ledger.getYearTotalPayment());
                    for (int i = 1; i <= 12; i++) {
                        // 反射获取费用金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(ledger, "getMonth", i));
                        // 反射获取付款金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(ledger, "getMonthPayment", i));
                        // 反射获取开票金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(ledger, "getMonthInvoice", i));
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用台账列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<Ledger> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<FeeTypeDict> feeTypeDictList = envFeeDictService.selectAllFeeType();
        Map<String, FeeTypeDict> feeTypeDictMap = new HashMap<>();
        feeTypeDictList.forEach( e -> feeTypeDictMap.put(e.getTypeCode(), e));
        list.forEach( e -> {
            String feeType = e.getFeeType();
            String feeTypeName = "";
            if (feeTypeDictMap.containsKey(feeType)) {
                feeTypeName = feeTypeDictMap.get(feeType).getTypeName();
            }
            e.setFeeTypeName(feeTypeName);
        });
    }

    @Override
    public AjaxResult selectEnvFeeInvoiceSellerLedgerList(EnvFeeLedgerReq req) {
        // 请求参数校验
        req = initReq(req);
        // 分页查询
        PageUtils.startPage();
        List<LedgerInvoiceSeller> list = envFeeLedgerMapper.selectEnvFeeInvoiceSellerLedgerList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void exportEnvFeeInvoiceSellerLedger(EnvFeeLedgerReq req, HttpServletResponse response) {
        // 请求参数校验
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保费用发票台账列表模板(销售方).xlsx");
            if (null == workbook) {
                return;
            }
            List<LedgerInvoiceSeller> list = envFeeLedgerMapper.selectEnvFeeInvoiceSellerLedgerList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 4;// 首行
                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (LedgerInvoiceSeller seller : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 销售方名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(seller.getSellerName());
                    // 年份
                    cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    cell.setCellValue(seller.getYearNum());
                    // 发票总额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, seller.getYearTotalInvoiceAmount());
                    // 税额总额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, seller.getYearTotalTaxAmount());
                    // 总金额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, seller.getYearTotalAmount());
                    for (int i = 1; i <= 12; i++) {
                        // 反射获取发票金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(seller, "getMonthInvoiceAmount", i));
                        // 反射获取税额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(seller, "getMonthTaxAmount", i));
                        // 反射获取总金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(seller, "getMonthTotalAmount", i));
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用发票台账列表(销售方).xlsx", StandardCharsets.UTF_8));
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
    public AjaxResult selectEnvFeeInvoiceBuyerLedgerList(EnvFeeLedgerReq req) {
        // 请求参数校验
        req = initReq(req);
        // 分页查询
        PageUtils.startPage();
        List<LedgerInvoiceBuyer> list = envFeeLedgerMapper.selectEnvFeeInvoiceBuyerLedgerList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void exportEnvFeeInvoiceBuyerLedger(EnvFeeLedgerReq req, HttpServletResponse response) {
        // 请求参数校验
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保费用发票台账列表模板(购买方).xlsx");
            if (null == workbook) {
                return;
            }
            List<LedgerInvoiceBuyer> list = envFeeLedgerMapper.selectEnvFeeInvoiceBuyerLedgerList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 4;// 首行
                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (LedgerInvoiceBuyer buyer : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 购买方名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(buyer.getBuyerName());
                    // 年份
                    cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    cell.setCellValue(buyer.getYearNum());
                    // 发票总额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, buyer.getYearTotalInvoiceAmount());
                    // 税额总额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, buyer.getYearTotalTaxAmount());
                    // 总金额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, buyer.getYearTotalAmount());
                    for (int i = 1; i <= 12; i++) {
                        // 反射获取发票金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(buyer, "getMonthInvoiceAmount", i));
                        // 反射获取税额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(buyer, "getMonthTaxAmount", i));
                        // 反射获取总金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(buyer, "getMonthTotalAmount", i));
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用发票台账列表(购买方).xlsx", StandardCharsets.UTF_8));
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
    public AjaxResult selectEnvFeePaymentPayerLedgerList(EnvFeeLedgerReq req) {
        // 请求参数校验
        req = initReq(req);
        // 分页查询
        PageUtils.startPage();
        List<LedgerPaymentPayer> list = envFeeLedgerMapper.selectEnvFeePaymentPayerLedgerList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void exportEnvFeePaymentPayerLedger(EnvFeeLedgerReq req, HttpServletResponse response) {
        // 请求参数校验
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保费用付款台账列表模板(付款方).xlsx");
            if (null == workbook) {
                return;
            }
            List<LedgerPaymentPayer> list = envFeeLedgerMapper.selectEnvFeePaymentPayerLedgerList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 4;// 首行
                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (LedgerPaymentPayer payer : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 付款方账户
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payer.getPayerAccount());
                    // 年份
                    cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    cell.setCellValue(payer.getYearNum());
                    // 支付总额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, payer.getYearPaymentAmount());
                    for (int i = 1; i <= 12; i++) {
                        // 反射获取支付金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(payer, "getMonthPaymentAmount", i));
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用付款台账列表(付款方).xlsx", StandardCharsets.UTF_8));
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
    public AjaxResult selectEnvFeePaymentPayeeLedgerList(EnvFeeLedgerReq req) {
        // 请求参数校验
        req = initReq(req);
        // 分页查询
        PageUtils.startPage();
        List<LedgerPaymentPayee> list = envFeeLedgerMapper.selectEnvFeePaymentPayeeLedgerList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void exportEnvFeePaymentPayeeLedger(EnvFeeLedgerReq req, HttpServletResponse response) {
        // 请求参数校验
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保费用付款台账列表模板(收款方).xlsx");
            if (null == workbook) {
                return;
            }
            List<LedgerPaymentPayee> list = envFeeLedgerMapper.selectEnvFeePaymentPayeeLedgerList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 4;// 首行
                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (LedgerPaymentPayee payee : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 收款方账户
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payee.getPayeeAccount());
                    // 年份
                    cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    cell.setCellValue(payee.getYearNum());
                    // 支付总额
                    cell = CellUtils.getCell(row, cellIndex++, styleN2);
                    setCellValue(cell, payee.getYearPaymentAmount());
                    for (int i = 1; i <= 12; i++) {
                        // 反射获取支付金额
                        cell = CellUtils.getCell(row, cellIndex++, styleN2);
                        setCellValue(cell, getValByMethod(payee, "getMonthPaymentAmount", i));
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用付款台账列表(收款方).xlsx", StandardCharsets.UTF_8));
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

    private EnvFeeLedgerReq initReq(EnvFeeLedgerReq req) {
        if (null == req) {
            req = new EnvFeeLedgerReq();
        }
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        if (StringUtils.isNotEmpty(req.getStart()) && StringUtils.isNotEmpty(req.getEnd())) {
            req.setStart(req.getStart() + " 00:00:00");
            req.setEnd(req.getEnd() + " 23:59:59");
        } else {
            req.setStart(null);
            req.setEnd(null);
        }
        // 创建月份Map，key为月份(1-12)，value为数字后缀，对应各报表实体类的字段后缀
        Map<Integer, String> months = new HashMap<>();
        months.put(1, "01");
        months.put(2, "02");
        months.put(3, "03");
        months.put(4, "04");
        months.put(5, "05");
        months.put(6, "06");
        months.put(7, "07");
        months.put(8, "08");
        months.put(9, "09");
        months.put(10, "10");
        months.put(11, "11");
        months.put(12, "12");
        req.setMonths(months);
        return req;
    }

    /**
     * 获取值
     */
    private BigDecimal getValByMethod(Object ledger, String getName, int index) {
        try {
            // 动态构建getter方法名
            String methodName = getName + String.format("%02d", index);
            // 使用反射调用getter方法
            Method method = ledger.getClass().getMethod(methodName);
            return (BigDecimal) method.invoke(ledger);
        } catch (Exception e) {
            log.error("Method get error", e);
        }
        return null;
    }

    private void setCellValue(Cell cell, BigDecimal decimal) {
        if (null == cell) {
            return;
        }
        if (null == decimal) {
            cell.setCellValue("/");
        } else {
            cell.setCellValue(decimal.doubleValue());
        }
    }
}


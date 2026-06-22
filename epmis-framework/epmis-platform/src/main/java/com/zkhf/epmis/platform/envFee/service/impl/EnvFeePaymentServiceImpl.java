package com.zkhf.epmis.platform.envFee.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envFee.dict.PaymentMethodDict;
import com.zkhf.epmis.platform.envFee.dict.PaymentStatusDict;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePayment;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePaymentReq;
import com.zkhf.epmis.platform.envFee.service.EnvFeeDictService;
import com.zkhf.epmis.platform.envFee.service.EnvFeePaymentService;
import com.zkhf.epmis.platform.envFee.service.EnvFeeService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envFee.EnvFeePaymentMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 费用付款信息Service业务层处理
 */
@Slf4j
@Service
public class EnvFeePaymentServiceImpl implements EnvFeePaymentService {

    private EnvFeePaymentMapper envFeePaymentMapper;
    @Autowired
    public void setEnvFeePaymentsMapper(EnvFeePaymentMapper envFeePaymentMapper) {
        this.envFeePaymentMapper = envFeePaymentMapper;
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

    private EnvFeeDictService envFeeDictService;
    @Autowired
    public void setEnvFeeDictService(EnvFeeDictService envFeeDictService) {
        this.envFeeDictService = envFeeDictService;
    }

    @Override
    public AjaxResult selectEnvFeePaymentListByFeeId(String feeId) {
        List<EnvFeePayment> list = envFeePaymentMapper.selectEnvFeePaymentListByFeeId(feeId);
        // 数据填充
        fill(list);
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult selectEnvFeePaymentList(EnvFeePaymentReq req) {
        if (null == req) {
            req = new EnvFeePaymentReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EnvFeePayment> list = envFeePaymentMapper.selectEnvFeePaymentList(req);
        // 数据填充
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "费用付款信息", businessType = BusinessType.EXPORT)
    public void exportEnvFeePayment(EnvFeePaymentReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvFeePaymentReq();
        }
        OutputStream outputStream = null;
        try {
            String templatePath = "template/环保费用付款列表模板.xlsx";
            InputStream fis = getClass().getClassLoader().getResourceAsStream(templatePath);
            if (fis == null) {
                log.error("无法从路径加载资源 {}", templatePath);
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvFeePayment> list = envFeePaymentMapper.selectEnvFeePaymentList(req);
            // 数据填充
            fill(list);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 2;// 首行

            CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
            CellStyle numStyle2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,2);
            if (null != list && !list.isEmpty()) {
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvFeePayment payment : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 付款流水号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getPaymentNumber());
                    // 付款金额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != payment.getPaymentAmount()) {
                        cell.setCellValue(payment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 付款日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != payment.getPaymentDate()) {
                        cell.setCellValue(payment.getPaymentDate().format(DateUtils.yy_m_d));
                    }
                    // 支付方式
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getPaymentMethod());
                    // 银行账户
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getBankAccount());
                    // 交易流水号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getTransactionNumber());
                    // 付款方账户
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getPayerAccount());
                    // 收款方账户
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getPayeeAccount());
                    // 付款状态
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getPaymentStatusDesc());
                    // 付款备注
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(payment.getPaymentRemark());
                    // 退款总金额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != payment.getRefundAmount()) {
                        cell.setCellValue(payment.getRefundAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 最后退款日期
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != payment.getRefundDate()) {
                        cell.setCellValue(payment.getRefundDate().format(DateUtils.yy_m_d));
                    }
                    // 退款备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(payment.getRefundRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用付款列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<EnvFeePayment> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 支付方式
        List<PaymentMethodDict> paymentMethodDictList = envFeeDictService.selectAllPaymentMethod();
        Map<String, PaymentMethodDict> paymentMethodDictMap = new HashMap<>();
        paymentMethodDictList.forEach( e -> paymentMethodDictMap.put(e.getMethodCode(), e));
        // 付款状态
        List<PaymentStatusDict> paymentStatusDictList = envFeeDictService.selectAllPaymentStatus();
        Map<String, PaymentStatusDict> paymentStatusDictMap = new HashMap<>();
        paymentStatusDictList.forEach( e -> paymentStatusDictMap.put(e.getStatusCode(), e));
        list.forEach( e -> {
            PaymentMethodDict paymentMethodDict = paymentMethodDictMap.get(e.getPaymentMethod());
            if (null != paymentMethodDict) {
                e.setPaymentMethodName(paymentMethodDict.getMethodName());
                e.setPaymentMethodDesc(paymentMethodDict.getDescription());
            }
            PaymentStatusDict paymentStatusDict = paymentStatusDictMap.get(e.getPaymentStatus());
            if (null != paymentStatusDict) {
                e.setPaymentStatusName(paymentStatusDict.getStatusName());
                e.setPaymentStatusDesc(paymentStatusDict.getDescription());
            }
        });
    }

    @Override
    @Log(title = "费用付款信息", businessType = BusinessType.INSERT)
    public AjaxResult insertEnvFeePayment(EnvFeePayment envFeePayment) {
        if (StringUtils.isEmpty(envFeePayment.getPaymentNumber())) {
            return AjaxResult.error("付款流水号不能为空");
        }
        // 校验付款流水号是否已存在
        int count = envFeePaymentMapper.checkExistsPaymentNumber(envFeePayment.getPaymentNumber());
        if (count > 0) {
            return AjaxResult.error("付款流水号已存在，请确认");
        }
        count = envFeePaymentMapper.insertEnvFeePayment(envFeePayment);
        if (count > 0 && null != envFeePayment.getAnnexIds() && !envFeePayment.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(envFeePayment.getPaymentNumber(), AnnexTypeEnum.envFeePayment.name(), envFeePayment.getAnnexIds());
        }
        if (count > 0) {
            // 费用登记中的付款金额更新
            envFeeService.updateEnvFeePaymentAmount(envFeePayment.getFeeId());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "费用付款信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvFeePayment(EnvFeePayment envFeePayment) {
        if (StringUtils.isEmpty(envFeePayment.getPaymentNumber())) {
            return AjaxResult.error("付款流水号不能为空");
        }
        int count = envFeePaymentMapper.updateEnvFeePayment(envFeePayment);
        if (count > 0) {
            annexService.updateAnnex(envFeePayment.getPaymentNumber(), AnnexTypeEnum.envFeePayment.name(), envFeePayment.getAnnexIds());
            // 费用登记中的付款金额更新
            envFeeService.updateEnvFeePaymentAmount(envFeePayment.getFeeId());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "费用付款信息", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnvFeePaymentById(String paymentNumber) {
        EnvFeePayment info = envFeePaymentMapper.selectEnvFeePaymentById(paymentNumber);
        if (null == info) {
            return AjaxResult.error("费用付款信息不存在");
        }
        int count = envFeePaymentMapper.deleteEnvFeePaymentById(paymentNumber);
        if (count > 0) {
            // 删除附件
            annexService.updateAnnex(paymentNumber, AnnexTypeEnum.envFeePayment.name(), null);
            // 费用登记中的付款金额更新
            envFeeService.updateEnvFeePaymentAmount(info.getFeeId());
        }
        return AjaxResult.success(count);
    }
}


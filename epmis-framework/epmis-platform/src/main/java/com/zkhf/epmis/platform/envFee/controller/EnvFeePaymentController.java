package com.zkhf.epmis.platform.envFee.controller;

import jakarta.servlet.http.HttpServletResponse;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePaymentReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePayment;
import com.zkhf.epmis.platform.envFee.service.EnvFeePaymentService;

/**
 * 费用付款信息Controller
 */
@RestController
@RequestMapping("/platform/fees/payments")
public class EnvFeePaymentController {

    private EnvFeePaymentService envFeePaymentService;
    @Autowired
    public void setEnvFeePaymentsService(EnvFeePaymentService envFeePaymentService) {
        this.envFeePaymentService = envFeePaymentService;
    }

    /**
     * 查询环保费用登记下的付款列表
     */
    @GetMapping("/listByFeeId/{feeId}")
    public AjaxResult listByFeeId(@PathVariable String feeId) {
        return envFeePaymentService.selectEnvFeePaymentListByFeeId(feeId);
    }

    /**
     * 查询费用付款信息列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvFeePaymentReq req) {
        return envFeePaymentService.selectEnvFeePaymentList(req);
    }

    /**
     * 导出费用付款信息列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvFeePaymentReq req, HttpServletResponse response) {
        envFeePaymentService.exportEnvFeePayment(req, response);
    }

    /**
     * 新增费用付款信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvFeePayment info) {
        return envFeePaymentService.insertEnvFeePayment(info);
    }

    /**
     * 修改费用付款信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvFeePayment info) {
        return envFeePaymentService.updateEnvFeePayment(info);
    }

    /**
     * 删除费用付款信息
     */
    @DeleteMapping("/{paymentNumber}")
    public AjaxResult remove(@PathVariable String paymentNumber) {
        return envFeePaymentService.deleteEnvFeePaymentById(paymentNumber);
    }
}

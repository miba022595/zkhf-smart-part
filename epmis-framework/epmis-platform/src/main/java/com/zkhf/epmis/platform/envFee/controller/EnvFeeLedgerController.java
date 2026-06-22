package com.zkhf.epmis.platform.envFee.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeLedgerReq;
import com.zkhf.epmis.platform.envFee.service.EnvFeeLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环保费用台账 Controller
 */
@RestController
@RequestMapping("/platform/fees/ledger")
public class EnvFeeLedgerController {

    private EnvFeeLedgerService envFeeLedgerService;
    @Autowired
    public void setEnvFeesLedgerService(EnvFeeLedgerService envFeeLedgerService) {
        this.envFeeLedgerService = envFeeLedgerService;
    }

    /**
     * 查询环保费用台账列表
     */
    @PostMapping("/list")
    public AjaxResult selectEnvFeeLedgerList(@RequestBody(required = false) EnvFeeLedgerReq req) {
        return envFeeLedgerService.selectEnvFeeLedgerList(req);
    }

    /**
     * 导出环保费用台账登记列表
     */
    @PostMapping("/exportTemplate")
    public void exportEnvFeeLedger(@RequestBody(required = false) EnvFeeLedgerReq req, HttpServletResponse response) {
        envFeeLedgerService.exportEnvFeeLedger(req, response);
    }

    /**
     * 查询环保费用发票台账列表-销售方台账
     */
    @PostMapping("/invoiceSeller/list")
    public AjaxResult selectEnvFeeInvoiceSellerLedgerList(@RequestBody(required = false) EnvFeeLedgerReq req) {
        return envFeeLedgerService.selectEnvFeeInvoiceSellerLedgerList(req);
    }

    /**
     * 导出环保费用发票台账登记列表-销售方台账
     */
    @PostMapping("/invoiceSeller/exportTemplate")
    public void exportEnvFeeInvoiceSellerLedger(@RequestBody(required = false) EnvFeeLedgerReq req, HttpServletResponse response) {
        envFeeLedgerService.exportEnvFeeInvoiceSellerLedger(req, response);
    }

    /**
     * 查询环保费用发票台账列表-购买方台账
     */
    @PostMapping("/invoiceBuyer/list")
    public AjaxResult selectEnvFeeInvoiceBuyerLedgerList(@RequestBody(required = false) EnvFeeLedgerReq req) {
        return envFeeLedgerService.selectEnvFeeInvoiceBuyerLedgerList(req);
    }

    /**
     * 导出环保费用发票台账登记列表-购买方台账
     */
    @PostMapping("/invoiceBuyer/exportTemplate")
    public void exportEnvFeeInvoiceBuyerLedger(@RequestBody(required = false) EnvFeeLedgerReq req, HttpServletResponse response) {
        envFeeLedgerService.exportEnvFeeInvoiceBuyerLedger(req, response);
    }

    /**
     * 查询环保费用付款台账列表-付款方台账
     */
    @PostMapping("/paymentPayer/list")
    public AjaxResult selectEnvFeePaymentPayerLedgerList(@RequestBody(required = false) EnvFeeLedgerReq req) {
        return envFeeLedgerService.selectEnvFeePaymentPayerLedgerList(req);
    }

    /**
     * 导出环保费用付款台账登记列表-付款方台账
     */
    @PostMapping("/paymentPayer/exportTemplate")
    public void exportEnvFeePaymentPayerLedger(@RequestBody(required = false) EnvFeeLedgerReq req, HttpServletResponse response) {
        envFeeLedgerService.exportEnvFeePaymentPayerLedger(req, response);
    }

    /**
     * 查询环保费用付款台账列表-收款方台账
     */
    @PostMapping("/paymentPayee/list")
    public AjaxResult selectEnvFeePaymentPayeeLedgerList(@RequestBody(required = false) EnvFeeLedgerReq req) {
        return envFeeLedgerService.selectEnvFeePaymentPayeeLedgerList(req);
    }

    /**
     * 导出环保费用付款台账登记列表-收款方台账
     */
    @PostMapping("/paymentPayee/exportTemplate")
    public void exportEnvFeePaymentPayeeLedger(@RequestBody(required = false) EnvFeeLedgerReq req, HttpServletResponse response) {
        envFeeLedgerService.exportEnvFeePaymentPayeeLedger(req, response);
    }

}

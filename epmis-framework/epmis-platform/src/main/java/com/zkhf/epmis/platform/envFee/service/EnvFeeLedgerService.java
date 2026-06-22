package com.zkhf.epmis.platform.envFee.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFee;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeLedgerReq;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环保费用台账Service接口
 */
public interface EnvFeeLedgerService {

    /**
     * 查询环保费用台账列表
     */
    AjaxResult selectEnvFeeLedgerList(EnvFeeLedgerReq req);

    /**
     * 导出环保费用台账登记列表
     */
    void exportEnvFeeLedger(EnvFeeLedgerReq req, HttpServletResponse response);

    /**
     * 查询环保费用发票台账列表-销售方台账
     */
    AjaxResult selectEnvFeeInvoiceSellerLedgerList(EnvFeeLedgerReq req);

    /**
     * 导出环保费用发票台账登记列表-销售方台账
     */
    void exportEnvFeeInvoiceSellerLedger(EnvFeeLedgerReq req, HttpServletResponse response);

    /**
     * 查询环保费用发票台账列表-购买方台账
     */
    AjaxResult selectEnvFeeInvoiceBuyerLedgerList(EnvFeeLedgerReq req);

    /**
     * 导出环保费用发票台账登记列表-购买方台账
     */
    void exportEnvFeeInvoiceBuyerLedger(EnvFeeLedgerReq req, HttpServletResponse response);

    /**
     * 查询环保费用付款台账列表-付款方台账
     */
    AjaxResult selectEnvFeePaymentPayerLedgerList(EnvFeeLedgerReq req);

    /**
     * 导出环保费用付款台账登记列表-付款方台账
     */
    void exportEnvFeePaymentPayerLedger(EnvFeeLedgerReq req, HttpServletResponse response);

    /**
     * 查询环保费用付款台账列表-收款方台账
     */
    AjaxResult selectEnvFeePaymentPayeeLedgerList(EnvFeeLedgerReq req);

    /**
     * 导出环保费用付款台账登记列表-收款方台账
     */
    void exportEnvFeePaymentPayeeLedger(EnvFeeLedgerReq req, HttpServletResponse response);
}

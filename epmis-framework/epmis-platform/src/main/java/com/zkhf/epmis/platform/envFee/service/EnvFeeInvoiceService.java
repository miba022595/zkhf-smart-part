package com.zkhf.epmis.platform.envFee.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoice;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoiceReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 费用发票信息Service接口
 */
public interface EnvFeeInvoiceService {

    /**
     * 查询环保费用登记下的发票列表
     */
    AjaxResult selectEnvFeeInvoiceListByFeeId(String feeId);

    /**
     * 查询费用发票信息列表
     */
    AjaxResult selectEnvFeeInvoiceList(EnvFeeInvoiceReq req);

    /**
     * 导出费用发票信息列表
     */
    void exportEnvFeeInvoice(EnvFeeInvoiceReq req, HttpServletResponse response);

    /**
     * 新增费用发票信息
     */
    AjaxResult insertEnvFeeInvoice(EnvFeeInvoice info);

    /**
     * 修改费用发票信息
     */
    AjaxResult updateEnvFeeInvoice(EnvFeeInvoice info);

    /**
     * 删除费用发票信息信息
     */
    AjaxResult deleteEnvFeeInvoiceById(String invoiceNumber);
}

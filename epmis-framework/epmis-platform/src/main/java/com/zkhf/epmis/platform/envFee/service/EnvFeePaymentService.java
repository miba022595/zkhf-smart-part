package com.zkhf.epmis.platform.envFee.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePayment;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePaymentReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 费用付款信息Service接口
 */
public interface EnvFeePaymentService {

    /**
     * 查询环保费用登记下的付款列表
     */
    AjaxResult selectEnvFeePaymentListByFeeId(String feeId);

    /**
     * 查询费用付款信息列表
     */
    AjaxResult selectEnvFeePaymentList(EnvFeePaymentReq req);

    /**
     * 导出费用付款信息列表
     */
    void exportEnvFeePayment(EnvFeePaymentReq req, HttpServletResponse response);

    /**
     * 新增费用付款信息
     */
    AjaxResult insertEnvFeePayment(EnvFeePayment info);

    /**
     * 修改费用付款信息
     */
    AjaxResult updateEnvFeePayment(EnvFeePayment info);

    /**
     * 删除费用付款信息信息
     */
    AjaxResult deleteEnvFeePaymentById(String paymentNumber);
}

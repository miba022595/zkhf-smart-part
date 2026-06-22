package com.zkhf.epmis.platform.envFee.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFee;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环保费用登记Service接口
 */
public interface EnvFeeService {

    /**
     * 查询环保费用登记列表
     */
    AjaxResult selectEnvFeeList(EnvFeeReq req);

    /**
     * 导出环保费用登记列表
     */
    void exportEnvFee(EnvFeeReq req, HttpServletResponse response);

    /**
     * 新增环保费用登记
     */
    AjaxResult insertEnvFee(EnvFee info);

    /**
     * 修改环保费用登记
     */
    AjaxResult updateEnvFee(EnvFee info);

    /**
     * 删除环保费用登记信息
     */
    AjaxResult deleteEnvFeeById(String feeId);

    /**
     * 开票金额更新
     */
    void updateEnvFeeInvoiceAmount(String feeId);

    /**
     * 付款金额更新
     */
    void updateEnvFeePaymentAmount(String feeId);
}

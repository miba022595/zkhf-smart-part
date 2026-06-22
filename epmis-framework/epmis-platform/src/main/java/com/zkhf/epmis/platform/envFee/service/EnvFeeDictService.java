package com.zkhf.epmis.platform.envFee.service;

import com.zkhf.epmis.platform.envFee.dict.*;

import java.util.List;
import java.util.Map;

/**
 * 环保费用登记各类型字典获取 Service接口
 */
public interface EnvFeeDictService {

    /**
     * 获取所有费用类型信息
     */
    List<FeeTypeDict> selectAllFeeType();

    /**
     * 获取所有缴费状态信息
     */
    List<FeeStatusDict> selectAllFeeStatus();

    /**
     * 获取所有支付方式信息
     */
    List<PaymentMethodDict> selectAllPaymentMethod();

    /**
     * 获取所有付款状态信息
     */
    List<PaymentStatusDict> selectAllPaymentStatus();

    /**
     * 获取所有发票类型信息
     */
    List<Map<String, Object>> selectAllInvoiceType();
}

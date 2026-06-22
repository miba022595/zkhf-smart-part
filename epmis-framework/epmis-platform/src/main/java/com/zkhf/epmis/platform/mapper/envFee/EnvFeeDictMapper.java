package com.zkhf.epmis.platform.mapper.envFee;

import com.zkhf.epmis.platform.envFee.dict.*;

import java.util.List;

/**
 * 环保费用登记Mapper接口
 */
public interface EnvFeeDictMapper {

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
}

package com.zkhf.epmis.platform.mapper.envFee;

import com.zkhf.epmis.platform.envFee.domain.EnvFeePayment;
import com.zkhf.epmis.platform.envFee.domain.EnvFeePaymentReq;

import java.util.List;

/**
 * 费用付款信息Mapper接口
 */
public interface EnvFeePaymentMapper {

    /**
     * 查询费用付款信息
     */
    EnvFeePayment selectEnvFeePaymentById(String paymentNumber);

    /**
     * 查询环保费用登记下的付款列表
     */
    List<EnvFeePayment> selectEnvFeePaymentListByFeeId(String feeId);

    /**
     * 查询费用付款信息列表
     */
    List<EnvFeePayment> selectEnvFeePaymentList(EnvFeePaymentReq req);

    /**
     * 校验付款流水号是否已存在
     */
    int checkExistsPaymentNumber(String paymentNumber);

    /**
     * 新增费用付款信息
     */
    int insertEnvFeePayment(EnvFeePayment info);

    /**
     * 修改费用付款信息
     */
    int updateEnvFeePayment(EnvFeePayment info);

    /**
     * 删除费用付款信息
     */
    int deleteEnvFeePaymentById(String paymentNumber);

}

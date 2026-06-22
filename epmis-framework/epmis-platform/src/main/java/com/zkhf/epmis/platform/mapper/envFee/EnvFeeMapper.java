package com.zkhf.epmis.platform.mapper.envFee;

import com.zkhf.epmis.platform.envFee.domain.EnvFee;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeReq;

import java.util.List;

/**
 * 环保费用登记Mapper接口
 */
public interface EnvFeeMapper {

    /**
     * 查询环保费用登记列表
     */
    List<EnvFee> selectEnvFeeList(EnvFeeReq req);

    /**
     * 新增环保费用登记
     */
    int insertEnvFee(EnvFee info);

    /**
     * 修改环保费用登记
     */
    int updateEnvFee(EnvFee info);

    /**
     * 查询费用登记是否存在付款信息
     */
    int selectFeePaymentCountByFeeId(String feeId);

    /**
     * 查询费用登记是否存在发票信息
     */
    int selectFeeInvoiceCountByFeeId(String feeId);

    /**
     * 删除环保费用登记
     */
    int deleteEnvFeeById(String feeId);

    /**
     * 开票金额更新
     */
    void updateEnvFeeInvoiceAmount(String feeId);

    /**
     * 付款金额更新
     */
    void updateEnvFeePaymentAmount(String feeId);

}

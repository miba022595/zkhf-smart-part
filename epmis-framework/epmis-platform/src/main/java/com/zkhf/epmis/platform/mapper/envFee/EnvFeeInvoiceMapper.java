package com.zkhf.epmis.platform.mapper.envFee;

import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoice;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoiceReq;

import java.util.List;

/**
 * 费用发票信息Mapper接口
 */
public interface EnvFeeInvoiceMapper {

    /**
     * 通过id查询费用发票信息
     */
    EnvFeeInvoice selectEnvFeeInvoiceById(String invoiceNumber);

    /**
     * 查询环保费用登记下的发票列表
     */
    List<EnvFeeInvoice> selectEnvFeeInvoiceListByFeeId(String feeId);

    /**
     * 查询费用发票信息列表
     */
    List<EnvFeeInvoice> selectEnvFeeInvoiceList(EnvFeeInvoiceReq req);

    /**
     * 校验是否已存在发票号码
     */
    int checkExistsInvoiceNumber(String invoiceNumber);

    /**
     * 新增费用发票信息
     */
    int insertEnvFeeInvoice(EnvFeeInvoice info);

    /**
     * 修改费用发票信息
     */
    int updateEnvFeeInvoice(EnvFeeInvoice info);

    /**
     * 删除费用发票信息
     */
    int deleteEnvFeeInvoiceById(String invoiceNumber);

}

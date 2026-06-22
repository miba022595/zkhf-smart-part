package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 费用付款信息对象 t_env_fee_payments
 */
@Data
public class EnvFeePaymentReq {

    /** 归属企业编码 */
    private String entCode;
    private List<String> entCodes;

    /** 费用记录ID */
    private String feeId;

    /** 付款日期-开始时间 */
    private LocalDate paymentStart;
    /** 付款日期-结束时间 */
    private LocalDate paymentEnd;

    /** 交易流水号 */
    private String transactionNumber;

    /** 付款状态 */
    private String paymentStatus;

}

package com.zkhf.epmis.platform.envFee.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用付款信息对象 t_env_fee_payments
 */
@Data
public class EnvFeePayment {

    /** 付款流水号 */
    private String paymentNumber;

    /** 费用记录ID */
    private String feeId;

    /** 付款金额 */
    private BigDecimal paymentAmount;

    /** 付款日期 */
    private LocalDate paymentDate;

    /** 支付方式（源自 t_bas_payment_method_dict）*/
    private String paymentMethod;
    private String paymentMethodName;
    private String paymentMethodDesc;

    /** 银行账户 */
    private String bankAccount;

    /** 交易流水号 */
    private String transactionNumber;

    /** 付款方账户 */
    private String payerAccount;

    /** 收款方账户 */
    private String payeeAccount;

    /** 付款状态（源自 t_bas_payment_status_dict） */
    private String paymentStatus;
    private String paymentStatusName;
    private String paymentStatusDesc;

    /** 付款备注 */
    private String paymentRemark;

    /** 退款总金额 */
    private BigDecimal refundAmount;

    /** 最后退款日期 */
    private LocalDate refundDate;

    /** 退款备注 */
    private String refundRemark;

    /**
     * 付款凭证附件附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}

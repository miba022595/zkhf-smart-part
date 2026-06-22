package com.zkhf.epmis.platform.envFee.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.platform.enums.InvoiceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用发票信息对象 t_env_fee_invoices
 */
@Data
public class EnvFeeInvoice {

    /** 发票状态：1-正常 2-已红冲 */
    public static final Integer INVOICE_NORMAL = 1;
    /** 发票状态：1-正常 2-已红冲 */
    public static final Integer INVOICE_RED_WASH = 2;

    /** 发票号码 */
    private String invoiceNumber;

    /** 费用记录ID */
    private String feeId;

    /** 发票代码 */
    private String invoiceCode;

    /** 发票金额（不含税） */
    private BigDecimal invoiceAmount;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 发票价税合计金额 */
    private BigDecimal totalAmount;

    /** 开票日期 */
    private LocalDate invoiceDate;

    /** 发票类型 {@link InvoiceType} */
    private String invoiceType;
    private String invoiceTypeDesc;

    /** 税率（单位：百分比，如13表示13%） */
    private Integer taxRate;

    /** 销售方名称 */
    private String sellerName;

    /** 销售方税号 */
    private String sellerTaxId;

    /** 发票状态：1-正常 2-已红冲 */
    private Integer invoiceStatus;

    /** 发票校验码 */
    private String checkCode;

    /** 发票备注 */
    private String remark;

    /**
     * 附件列表（更新时用）
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

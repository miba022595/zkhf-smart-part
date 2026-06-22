package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 费用发票信息请求对象
 */
@Data
public class EnvFeeInvoiceReq {

    /** 费用记录ID */
    private String feeId;

    /** 归属企业编码 */
    private String entCode;
    private List<String> entCodes;

    /** 发票号码（模糊） */
    private String invoiceNumber;

    /** 开票日期-开始时间 */
    private LocalDate invoiceStart;
    /** 开票日期-结束时间 */
    private LocalDate invoiceEnd;

    /** 发票类型 */
    private String invoiceType;

    /** 发票状态：1-正常 2-已红冲 */
    private Integer invoiceStatus;

}

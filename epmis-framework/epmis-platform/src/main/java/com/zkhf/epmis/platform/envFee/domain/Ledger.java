package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 环保费用统计-实体类
 */
@Data
public class Ledger {

    /**
     * 费用类型
     */
    private String feeType;
    private String feeTypeName;

    /**
     * 年份
     */
    private Integer yearNum;

    // 1-9月费用金额
    private BigDecimal month01;
    private BigDecimal month02;
    private BigDecimal month03;
    private BigDecimal month04;
    private BigDecimal month05;
    private BigDecimal month06;
    private BigDecimal month07;
    private BigDecimal month08;
    private BigDecimal month09;

    // 10-12月费用金额
    private BigDecimal month10;
    private BigDecimal month11;
    private BigDecimal month12;

    // 1-9月发票金额
    private BigDecimal monthInvoice01;
    private BigDecimal monthInvoice02;
    private BigDecimal monthInvoice03;
    private BigDecimal monthInvoice04;
    private BigDecimal monthInvoice05;
    private BigDecimal monthInvoice06;
    private BigDecimal monthInvoice07;
    private BigDecimal monthInvoice08;
    private BigDecimal monthInvoice09;

    // 10-12月发票金额
    private BigDecimal monthInvoice10;
    private BigDecimal monthInvoice11;
    private BigDecimal monthInvoice12;

    // 1-9月支付金额
    private BigDecimal monthPayment01;
    private BigDecimal monthPayment02;
    private BigDecimal monthPayment03;
    private BigDecimal monthPayment04;
    private BigDecimal monthPayment05;
    private BigDecimal monthPayment06;
    private BigDecimal monthPayment07;
    private BigDecimal monthPayment08;
    private BigDecimal monthPayment09;

    // 10-12月支付金额
    private BigDecimal monthPayment10;
    private BigDecimal monthPayment11;
    private BigDecimal monthPayment12;

    /**
     * 年度总费用金额
     */
    private BigDecimal yearTotalFee;

    /**
     * 年度总发票金额
     */
    private BigDecimal yearTotalInvoice;

    /**
     * 年度总支付金额
     */
    private BigDecimal yearTotalPayment;
}

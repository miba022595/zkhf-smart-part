package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 环保费用发票统计-销售方-实体类
 */
@Data
public class LedgerInvoiceSeller {

    /**
     * 销售方税号
     */
    private String sellerTaxId;

    /**
     * 销售方名称
     */
    private String sellerName;

    /**
     * 年份
     */
    private Integer yearNum;

    // 1-9月发票金额
    private BigDecimal monthInvoiceAmount01;
    private BigDecimal monthInvoiceAmount02;
    private BigDecimal monthInvoiceAmount03;
    private BigDecimal monthInvoiceAmount04;
    private BigDecimal monthInvoiceAmount05;
    private BigDecimal monthInvoiceAmount06;
    private BigDecimal monthInvoiceAmount07;
    private BigDecimal monthInvoiceAmount08;
    private BigDecimal monthInvoiceAmount09;
    // 10-12月发票金额
    private BigDecimal monthInvoiceAmount10;
    private BigDecimal monthInvoiceAmount11;
    private BigDecimal monthInvoiceAmount12;

    // 1-9月税额
    private BigDecimal monthTaxAmount01;
    private BigDecimal monthTaxAmount02;
    private BigDecimal monthTaxAmount03;
    private BigDecimal monthTaxAmount04;
    private BigDecimal monthTaxAmount05;
    private BigDecimal monthTaxAmount06;
    private BigDecimal monthTaxAmount07;
    private BigDecimal monthTaxAmount08;
    private BigDecimal monthTaxAmount09;

    // 10-12月税额
    private BigDecimal monthTaxAmount10;
    private BigDecimal monthTaxAmount11;
    private BigDecimal monthTaxAmount12;

    // 1-9月总金额（发票金额+税额）
    private BigDecimal monthTotalAmount01;
    private BigDecimal monthTotalAmount02;
    private BigDecimal monthTotalAmount03;
    private BigDecimal monthTotalAmount04;
    private BigDecimal monthTotalAmount05;
    private BigDecimal monthTotalAmount06;
    private BigDecimal monthTotalAmount07;
    private BigDecimal monthTotalAmount08;
    private BigDecimal monthTotalAmount09;

    // 10-12月总金额（发票金额+税额）
    private BigDecimal monthTotalAmount10;
    private BigDecimal monthTotalAmount11;
    private BigDecimal monthTotalAmount12;

    /**
     * 年度发票总额
     */
    private BigDecimal yearTotalInvoiceAmount;

    /**
     * 年度税额总额
     */
    private BigDecimal yearTotalTaxAmount;

    /**
     * 年度总金额
     */
    private BigDecimal yearTotalAmount;
}

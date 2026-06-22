package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 环保费用付款统计-付款方-实体类
 */
@Data
public class LedgerPaymentPayer {

    /**
     * 付款方账户
     */
    private String payerAccount;

    /**
     * 年份
     */
    private Integer yearNum;

    // 1-9月支付金额
    private BigDecimal monthPaymentAmount01;
    private BigDecimal monthPaymentAmount02;
    private BigDecimal monthPaymentAmount03;
    private BigDecimal monthPaymentAmount04;
    private BigDecimal monthPaymentAmount05;
    private BigDecimal monthPaymentAmount06;
    private BigDecimal monthPaymentAmount07;
    private BigDecimal monthPaymentAmount08;
    private BigDecimal monthPaymentAmount09;

    // 10-12月支付金额
    private BigDecimal monthPaymentAmount10;
    private BigDecimal monthPaymentAmount11;
    private BigDecimal monthPaymentAmount12;

    /**
     * 年度支付总额
     */
    private BigDecimal yearPaymentAmount;
}

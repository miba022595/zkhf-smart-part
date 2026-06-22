package com.zkhf.epmis.process.statistics.domain;

import lombok.Data;

/**
 * 图表结构-数据
 */
@Data
public class EffectiveTransData {

    /**
     * 传输实收量
     */
    private int realTrans;
    /**
     * 传输应收量
     */
    private int mustTrans;

    /**
     * 有效实收量
     */
    private int realValid;
    /**
     * 有效应收量
     */
    private int mustValid;

    /**
     * 数据传输率
     */
    private Float transRate;
    /**
     * 数据有效率
     */
    private Float validRate;

    /**
     * 数据有效传输率
     */
    private Float effectiveTransRate;

    /**
     * 污染物监测时间，yyyy-MM-dd
     */
    private String monitorTime;
}
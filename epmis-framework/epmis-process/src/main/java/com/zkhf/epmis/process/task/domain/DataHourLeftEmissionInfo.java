package com.zkhf.epmis.process.task.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小时剩余排放控制表
 */
@Data
public class DataHourLeftEmissionInfo {

    /**
     * 排口id
     */
    private String outPutId;

    /**
     * 污染因子编码
     */
    private String pollutantCode;

    /**
     * 污染物最大值
     */
    private BigDecimal standardValue;

    /**
     * 当前小时污染物累计平均值
     */
    private BigDecimal avgValue;

    /**
     * 当前小时剩余控制平均值
     */
    private BigDecimal surplusValue;
}
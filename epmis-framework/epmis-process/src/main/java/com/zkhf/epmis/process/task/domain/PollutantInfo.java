package com.zkhf.epmis.process.task.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 污染物信息
 */
@Data
public class PollutantInfo {

    /**
     * 污染物名称
     */
    private String pollutantCode;
    private String pollutantName;

    /**
     * 污染物指定时间内平均值
     */
    private BigDecimal avgValue;

    /**
     * 污染物本年时间内总值
     */
    private BigDecimal sumValue;

    /**
     * 污染物监测时间
     */
    private String monitorTime;
}
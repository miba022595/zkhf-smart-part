package com.zkhf.epmis.process.task.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 排放量记录对象 t_ent_annual_emission
 */
@Data
public class EmissionTask {

    /**
     * 企业code
     */
    private String entCode;

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 排放年份
     */
    private Integer emissionYear;

    /**
     * 污染因子编码
     */
    private String pollutantCode;

    /**
     * 年排放量
     */
    private BigDecimal emissions;
}

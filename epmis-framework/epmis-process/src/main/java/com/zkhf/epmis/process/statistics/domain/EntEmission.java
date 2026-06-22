package com.zkhf.epmis.process.statistics.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 企业年排量信息记录对象 t_ent_annual_emission
 */
@Data
public class EntEmission {

    /** 报警类型， 0正常，1预警，2报警 */
    public static final Integer NORMAL = 0;
    /** 报警类型， 0正常，1预警，2报警 */
    public static final Integer WARN = 1;
    /** 报警类型， 0正常，1预警，2报警 */
    public static final Integer ALARM = 2;

    /**
     * 企业编码
     */
    private String entCode;
    private String entName;

    /**
     * 排放年份
     */
    private Integer emissionYear;

    /**
     * 污染因子编码
     */
    private String pollutantCode;
    private String pollutantNameCn;
    private String pollutantNameEn;
    /**
     * 排放量单位
     */
    private String unitPfCn;
    private String unitPfEn;

    /**
     * 年排放量
     */
    private BigDecimal emissions;

    /**
     * 年排放量限值
     */
    private BigDecimal yLimit;

    /**
     * 截至到当前月的放量限值(截止当前月，包含)
     */
    private BigDecimal mLimit;

    /**
     * 报警类型， 0正常，1预警，2报警
     */
    private Integer alarm;
}

package com.zkhf.epmis.process.base.domain;

import lombok.Data;

/**
 * 数采报文对应的污染因子关系 2017版本和2003版对象 t_bas_pollutant_code
 */
@Data
public class PollutantCode {

    /**
     * 污染因子实际编码--以HJ-2017协议为主
     */
    private String pollutantCode;

    /**
     * 2017版报文编码
     */
    private String code2017;

    /**
     * 2005版报文编码
     */
    private String code2005;

    /**
     * 污染因子名称--英文
     */
    private String pollutantNameEn;

    /**
     * 污染因子名称--中文
     */
    private String pollutantNameCn;

    /**
     * 污染因子类型：1：废水；2：废气；
     */
    private Long pollutantType;

    /**
     * 污染因子单位--英文
     */
    private String pollutantUnitEn;

    /**
     * 污染因子单位--中文
     */
    private String pollutantUnitCn;

    /**
     * 排放量单位--中文
     */
    private String unitPfCn;

    /**
     * 排放量单位--英文
     */
    private String unitPfEn;

    /**
     * 使用状态：0：未使用；1：正在使用
     */
    private Long pollutantStatus;

    /**
     * 排序码
     */
    private Long pollutantSort;

    /**
     * 监测因子
     */
    private Object monFactor;
}

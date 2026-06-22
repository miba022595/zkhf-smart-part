package com.zkhf.epmis.platform.base.domain;

import lombok.Data;

/**
 * 数采报文对应的污染因子关系 2017版本和2003版对象 t_bas_pollutant_code
 */
@Data
public class PollutantCodeReq {

    /**
     * 污染因子实际编码--以HJ-2017协议为主
     */
    private String pollutantCode;

    /**
     * 污染因子类型：1：废水；2：废气；
     */
    private Integer pollutantType;

    /**
     * 使用状态：0：未使用；1：正在使用
     */
    private Integer pollutantStatus;
}

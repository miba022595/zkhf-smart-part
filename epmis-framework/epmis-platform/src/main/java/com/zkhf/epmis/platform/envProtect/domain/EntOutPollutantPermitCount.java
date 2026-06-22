package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

/**
 * 企业排污许可总量基础对象 t_bas_ent_out_pollutant_permit_total
 */
@Data
public class EntOutPollutantPermitCount {

    /**
     * 排污许可总量id
     */
    private String pollPermitCountId;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 污染物类别，1：废水；2：废气；3：无组织
     */
    private Integer pollType;

    /**
     * 污染因子编码
     */
    private String pollutantCode;

    /**
     * 污染因子名称
     */
    private String pollutantNameCn;
    private String pollutantNameEn;

    /**
     * 年份
     */
    private Integer permitYear;

    /**
     * 许可总量
     */
    private Double permitCount;

    /**
     * 累计单位
     */
    private String couUnit;
}

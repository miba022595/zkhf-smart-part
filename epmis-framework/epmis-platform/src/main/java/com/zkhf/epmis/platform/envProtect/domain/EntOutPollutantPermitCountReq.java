package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业排污许可总量基础对象 t_bas_ent_out_pollutant_permit_total
 */
@Data
public class EntOutPollutantPermitCountReq {

    /**
     * 企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 污染因子名称（模糊）
     */
    private String pollutantNameCn;
    private String pollutantNameEn;

    /**
     * 年份
     */
    private Integer permitYear;
}

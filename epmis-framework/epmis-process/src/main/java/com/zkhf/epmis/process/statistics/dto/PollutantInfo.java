package com.zkhf.epmis.process.statistics.dto;

import lombok.Data;

/**
 * 污染物信息
 */
@Data
public class PollutantInfo {

    /**
     * 污染因子实际编码
     */
    private String pollutantCode;

    /**
     * 污染因子名称--中文
     */
    private String pollutantNameCn;

    /**
     * 污染因子单位--中文
     */
    private String pollutantUnitCn;

    /**
     * 污染因子名称--英文
     */
    private String pollutantNameEn;

    /**
     * 污染因子单位--英文
     */
    private String pollutantUnitEn;

}
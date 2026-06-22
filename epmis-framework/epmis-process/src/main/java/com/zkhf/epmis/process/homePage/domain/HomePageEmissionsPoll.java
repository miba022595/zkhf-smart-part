package com.zkhf.epmis.process.homePage.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 污染物因子数据
 */
@Data
@Builder
public class HomePageEmissionsPoll {

    /**
     * 污染因子编码
     */
    private String pollutantCode;

    /**
     * 污染因子名称--英文
     */
    private String pollutantNameEn;

    /**
     * 污染因子名称--中文
     */
    private String pollutantNameCn;

    /**
     * 污染物浓度限值
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

    /**
     * 分钟数据列表
     */
    private List<Map<String, Object>> dataList;
}

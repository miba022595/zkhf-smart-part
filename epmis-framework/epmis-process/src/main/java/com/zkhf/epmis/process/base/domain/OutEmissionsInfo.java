package com.zkhf.epmis.process.base.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 排口排放量信息
 */
@Data
public class OutEmissionsInfo {

    /**
     * 排口id
     */
    private String outPutId;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 废气排口编码
     */
    private String outPutCode;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 污染因子编码
     */
    private String pollutantCode;

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
}

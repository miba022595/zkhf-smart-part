package com.zkhf.epmis.process.statistics.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 排口年排量信息详情记录对象
 */
@Data
public class OutEmission {

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 年排放量
     */
    private BigDecimal emissions;
    /**
     * 排放量单位
     */
    private String unitPfCn;
    private String unitPfEn;

    /**
     * 排口编码
     */
    private String outPutCode;
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
    private String outPutTypeDesc;
}

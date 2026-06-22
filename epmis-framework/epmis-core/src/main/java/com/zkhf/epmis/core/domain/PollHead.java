package com.zkhf.epmis.core.domain;

import lombok.Data;

import java.util.List;

/**
 * 污染物表头
 */
@Data
public class PollHead {

    /**
     * 污染物编码
     */
    private String pollCode;
    private String pollName;

    /**
     * 计量单位
     */
    private String rtdUnit;

    /**
     * 累计单位
     */
    private String couUnit;

    /**
     * 小数点位数，默认3
     */
    private Integer decimalPlaces;

    /**
     * 选择的监测因子
     */
    private List<HeadInfo> headList;
}

package com.zkhf.epmis.process.onlineMonitoring.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 多排口响应数据体
 */
@Data
@Builder
public class MultipleChartSeriesData {

    /** 排口主键id */
    private String outPutId;

    /** 数据列表 */
    private List<BigDecimal> list;
}

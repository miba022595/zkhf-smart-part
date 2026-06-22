package com.zkhf.epmis.process.onlineMonitoring.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 多排口响应数据体
 */
@Data
@Builder
public class MultipleChartSeries {

    /** 污染物显示名称 */
    private String name;

    /** 污染物单位 */
    private String unit;

    /** 数据列表 */
    private List<MultipleChartSeriesData> item;
}

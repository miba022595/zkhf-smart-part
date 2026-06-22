package com.zkhf.epmis.process.onlineMonitoring.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 排口在线检测请求消息
 */
@Data
@Builder
public class OutPutOnlineChartSeries {

    /** 污染物显示名称 */
    private String name;

    /** 污染物标准值 */
    private BigDecimal norm;

    /** 污染物单位 */
    private String unit;

    /** 数据列表 */
    private List<BigDecimal> list;
}

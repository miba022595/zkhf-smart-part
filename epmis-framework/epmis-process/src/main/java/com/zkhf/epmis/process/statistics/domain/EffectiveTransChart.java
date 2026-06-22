package com.zkhf.epmis.process.statistics.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业数据有效率信息表
 * 有效传输率=有效率*传输率
 * 摘自：《污染物排放自动监测设备标记规则》.pdf
 * 图表结构
 */
@Data
public class EffectiveTransChart {

    /**
     * 企业编码
     */
    private String entCode;
    private String entName;

    /**
     * 排口编码
     */
    private String outPutCode;
    private String outPutName;

    /**
     * 污染物类型，1废水、2废气
     */
    private Integer pollutionType;

    /**
     * 数据来源；1 小时传输、2 天传输
     */
    private Integer dataType;

    /**
     * 数据列表
     */
    private List<EffectiveTransData> dataList;
}
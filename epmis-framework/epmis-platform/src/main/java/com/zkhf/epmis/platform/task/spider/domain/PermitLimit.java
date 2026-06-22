package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

/**
 * 许可排放限值
 */
@Data
public class PermitLimit {
    /** 污染物名称 */
    private String pollutant;
    /** 排放浓度限值 */
    private String concentrationLimit;
    /** 排放总量限值 */
    private String totalLimit;
    /** 排放口 */
    private String outlet;
    /** 执行标准 */
    private String standard;
}
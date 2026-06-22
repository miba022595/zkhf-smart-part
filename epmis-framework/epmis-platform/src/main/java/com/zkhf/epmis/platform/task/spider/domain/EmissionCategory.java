package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

/**
 * 排放类别（按排放类型分类：废气/废水/扬尘/VOC/恶臭/噪声）
 */
@Data
public class EmissionCategory {
    /** 排放类型：废气/废水/扬尘/VOC/恶臭/噪声 */
    private String type;
    /** 主要污染物（逗号分隔） */
    private String pollutants;
    /** 排放规律 */
    private String emissionPattern;
    /** 执行标准 */
    private String execStandard;
}
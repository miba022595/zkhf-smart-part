package com.zkhf.epmis.process.envManual.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 环境手工检测任务报告数据列表
 */
@Data
public class EnvManualCheckReportDetail {

    /** 检测任务主键id */
    private String taskId;

    /**
     * 排放口名称
     */
    private String outPutName;
    private String outPutId;

    /** 数据标识id，check_frequency+sample_date */
    private String outId;

    /** 采样日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sampleDate;

    /**
     * 排口关联污染物
     */
    private String pollutantCodeName;
    private String pollutantCode;

    /** 检测频次 */
    private String checkFrequencyDesc;
    private Integer checkFrequency;

    /** 本期运行时间(h) */
    private Integer operationHour;

    /**
     * 污染信息
     */
    private String dataInfo;

    /**
     * 监测值-最大值
     */
    private BigDecimal max;

    /**
     * 监测值-最小值
     */
    private BigDecimal min;

    /**
     * 监测值-平均值
     */
    private BigDecimal avg;

    /**
     * 监测值-折算值
     */
    private BigDecimal zsAvg;
}

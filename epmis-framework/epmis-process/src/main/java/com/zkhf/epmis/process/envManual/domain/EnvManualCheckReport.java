package com.zkhf.epmis.process.envManual.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 环境手工检测任务报告对象
 */
@Data
public class EnvManualCheckReport {

    /** 报告名称 */
    private String reportName;

    /** 报告编号 */
    private String reportCode;

    /** 监测类别 */
    private String monitorCategory;

    /** 采样人员 */
    private String samplingPerson;

    /** 采样日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sampleDate;

    /** 分析/监测人员 */
    private String analysisPerson;

    /** 报告日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 分析/监测周期-开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate analysisStartDate;

    /** 分析/监测周期-结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate analysisEndDate;

    /**
     * 检测报告数据列表
     */
    private List<EnvManualCheckReportDetail> detailList;

    /**
     * 任务报告附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

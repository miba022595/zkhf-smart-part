package com.zkhf.epmis.platform.envManual.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 环境手工检测任务对象 t_env_manual_check_task
 */
@Data
public class EnvManualCheckTask {

    /** 检测任务主键id */
    private String taskId;

    /** 排口污染物主键id */
    private String outPutPollId;

    /** 任务日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskDate;

    /** 计划检测频次 {@link com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType}*/
    private Integer checkFrequency;
    private String checkFrequencyDesc;

    /** 执行标准 */
    private String executionStandard;

    /**
     * 超标上限
     */
    private Double overMaxvalue;

    /**
     * 超标下限
     */
    private Double overMinvalue;

    /** 检测单位（第三方单位） */
    private String monitorUnit;
    private String monitorUnitName;

    /** 任务描述 */
    private String taskDesc;

    /** 报告名称 */
    private String reportName;

    /** 报告编号 */
    private String reportCode;

    /** 监测类别 */
    private String monitorCategory;

    /** 采样日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sampleDate;

    /** 报告日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    /** 分析/监测周期-开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate analysisStartDate;

    /** 分析/监测周期-结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate analysisEndDate;

    /** 分析/监测人员 */
    private String analysisPerson;

    /** 采样人员 */
    private String samplingPerson;

    /** 本期运行时间(h) */
    private Integer operationHour;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 任务状态 {@link com.zkhf.epmis.platform.envManual.enums.TaskStatusType} */
    private Integer status;
    private String statusDesc;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 排放口名称
     */
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 排口关联污染物code
     */
    private String pollutantCode;
    private String pollutantNameCn;
    private String pollutantNameEn;

    /**
     * 任务报告附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

}

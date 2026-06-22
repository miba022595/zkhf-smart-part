package com.zkhf.epmis.platform.envManual.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 环境手工检测计划对象 t_env_manual_check_plan
 */
@Data
public class EnvManualCheckPlan {

    /** 排口污染物主键id */
    private String outPutPollId;

    /** 执行标准 */
    private String executionStandard;

    /**
     * 计划首次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstDate;

    /** 计划检测频次（1日次、2周次、3月次、4季度、5半年、6年、7两年） {@link CheckFrequencyType}*/
    private String checkFrequency;
    private String checkFrequencyDesc;

    /** 检测计划描述 */
    private String planDesc;

    /** 提交审核内容 */
    private String submitContent;

    /** 审批意见 */
    private String approvalOpinion;

    /** 计划状态 {@link com.zkhf.epmis.platform.envManual.enums.PlanStatusType}*/
    private Integer status;
    private String statusDesc;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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
     * 超标上限
     */
    private Double overMaxvalue;

    /**
     * 超标下限
     */
    private Double overMinvalue;

    /**
     * 执行标准附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

}

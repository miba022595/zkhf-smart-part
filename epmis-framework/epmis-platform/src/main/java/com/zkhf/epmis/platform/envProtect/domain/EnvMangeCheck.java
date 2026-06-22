package com.zkhf.epmis.platform.envProtect.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 企业环评环保管理-环保验收对象 t_env_mange_check
 */
@Data
public class EnvMangeCheck {
    /** 是否需验收批复，0否，1是 */
    public static final Integer CHECK_REPLY_Y = 1;
    /** 是否需验收批复，0否，1是 */
    public static final Integer CHECK_REPLY_N = 0;

    /**
     * 环评环保管理-环评主键id
     */
    private String checkId;

    /**
     * 企业编码
     */
    private String entCode;
    private String entName;

    /**
     * 环保验收名称
     */
    private String checkName;

    /**
     * 是否需验收批复，0否，1是
     */
    private Integer checkReply;

    /**
     * 审批部门
     */
    private String approvalDepart;

    /**
     * 批复文号
     */
    private String replyNo;

    /**
     * 验收监测机构
     */
    private String checkAgency;

    /**
     * 验收监测时间-开始时间
     */
    private LocalDate checkBeginTime;

    /**
     * 验收监测时间-结束时间
     */
    private LocalDate checkEndTime;

    /**
     * 验收报告专家评审时间
     */
    private LocalDate reviewTime;

    /**
     * 验收报告专家评审主要问题
     */
    private String reviewIssue;

    /**
     * 验收报告公式地址
     */
    private String recordAddress;

    /**
     * 验收报告公式开始时间
     */
    private LocalDate checkRecordBeginTime;

    /**
     * 验收报告公式截至时间
     */
    private LocalDate checkRecordEndTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 环保验收关联的项目列表
     */
    private List<String> projectIdList;
}

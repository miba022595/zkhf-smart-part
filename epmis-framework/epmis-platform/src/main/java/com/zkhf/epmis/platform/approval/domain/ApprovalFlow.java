package com.zkhf.epmis.platform.approval.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批流程定义对象 t_bas_approval_flow
 */
@Data
public class ApprovalFlow {
    /** 启用的审批流 */
    public static Integer ACTIVE = 1;
    public static Integer ACTIVE_NO = 0;
    /** 默认的审批流 */
    public static Integer DEFAULT_FLOW = 0;
    public static Integer DEFAULT_FLOW_NO = 1;
    /** 业务类型的字典 */
    public static String BUSINESS_TYPE_DICT = "approval_flow_business_type";


    /** 流程主键ID */
    private String flowId;

    /** 关联企业id */
    private String entCode;
    private String entName;

    /** 流程名称 */
    private String flowName;

    /**
     * 业务类型
     * 参考字典：approval_flow_business_type
     */
    private String businessType;
    private String businessTypeDesc;

    /** 开始节点ID，指向流程的第一个审批节点 */
    private String startNodeId;
    private String startNodeName;

    /** 流程版本号，用于版本控制 */
    private String version;

    /** 是否启用：0-停用，1-启用 */
    private Integer active;

    /** 是否默认审批流：0-非默认，1-默认 */
    private Integer defaultFlow;

    /** 流程描述 */
    private String description;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}

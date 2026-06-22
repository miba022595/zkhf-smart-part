package com.zkhf.epmis.platform.approval.domain;

import lombok.Data;

import java.util.List;

/**
 * 审批流程定义对象 t_bas_approval_flow
 */
@Data
public class ApprovalFlowReq {

    /** 关联企业id */
    private String entCode;
    private List<String> entCodes;

    /** 流程名称-模糊筛选 */
    private String flowName;

    /**
     * 业务类型：
     * 参考字典：approval_flow_business_type
     */
    private String businessType;

    /** 流程版本号，全匹配 */
    private String version;

    /** 是否启用：0-停用，1-启用 */
    private Integer active;

    /** 是否默认审批流：0-非默认，1-默认 */
    private Integer defaultFlow;
}

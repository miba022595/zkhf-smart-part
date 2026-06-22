package com.zkhf.epmis.platform.approval.domain;

import lombok.Data;

import java.util.List;

/**
 * 审批实例：存储具体的审批实例信息对象 t_bas_approval_instance
 */
@Data
public class ApprovalInstanceReq {

    /**
     * 业务类型：
     * 参考字典：approval_flow_business_type
     */
    private String businessType;

    /** 审批状态：PROCESSING-审批中，APPROVED-已通过，REJECTED-已拒绝，CANCELLED-已取消 */
    private String status;

    /** 关联企业id */
    private String entCode;
    private List<String> entCodes;
}

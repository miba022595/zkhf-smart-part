package com.zkhf.epmis.platform.approval.service;

import com.zkhf.epmis.core.enums.GenApprovalType;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstance;
import com.zkhf.epmis.platform.approval.enums.ApprovalInstanceStatus;

/**
 * 审批实例：审批通过调用修改原业务的状态
 */
public interface ApprovalService {

    /**
     * 审批流完成后调用
     */
    void approval(ApprovalInstance instance);

    /**
     * 审批流状态变更为公共的审批状态
     */
    default Integer statusTransToGenApprovalType(String status) {
        Integer newStatus = null;
        if (ApprovalInstanceStatus.PROCESSING.code.equals(status)) {
            newStatus = GenApprovalType.REVIEWING.code;
        } else if (ApprovalInstanceStatus.APPROVED.code.equals(status)) {
            newStatus = GenApprovalType.APPROVED.code;
        } else if (ApprovalInstanceStatus.REJECTED.code.equals(status)) {
            newStatus = GenApprovalType.REJECTED.code;
        } else if (ApprovalInstanceStatus.CANCELLED.code.equals(status)) {
            newStatus = GenApprovalType.CANCELLED.code;
        }
        return newStatus;
    }
}

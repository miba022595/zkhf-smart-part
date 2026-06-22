package com.zkhf.epmis.platform.approval.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.approval.domain.ApprovalHistory;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstance;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstanceReq;

import java.util.List;

/**
 * 审批实例：存储具体的审批实例信息Service接口
 */
public interface ApprovalInstanceService {

    /**
     * 查询审批实例：存储具体的审批实例信息
     */
    AjaxResult selectApprovalInstanceByBusiness(String businessType, String businessKey);

    /**
     * 待审批统计列表
     */
    AjaxResult countProcessingStatus();

    /**
     * 查询审批实例：存储具体的审批实例信息列表
     */
    AjaxResult selectApprovalInstanceList(ApprovalInstanceReq req);

    /**
     * 获取历史审批信息
     */
    List<ApprovalHistory> selectApprovalInstanceHistoryList(String businessType, String businessKey);

    /**
     * 提交审批，新增审批实例：存储具体的审批实例信息
     */
    AjaxResult insertApprovalInstance(ApprovalInstance info);

    /**
     * 相关人员进行审批工作，修改审批实例：存储具体的审批实例信息
     */
    AjaxResult updateApprovalInstance(ApprovalInstance info);

    /**
     * 查看消息的列表（可用于看待阅列表）
     */
    AjaxResult selectApprovalInstanceMessageList(Integer pushStatus);

    /**
     * 消息修改，可变更为已阅
     */
    AjaxResult approvalInstanceMessageNoted(Long messageId);
}

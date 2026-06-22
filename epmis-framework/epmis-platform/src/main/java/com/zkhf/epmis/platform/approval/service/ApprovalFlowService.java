package com.zkhf.epmis.platform.approval.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlow;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlowReq;
import com.zkhf.epmis.platform.approval.domain.ApprovalNode;

import java.util.List;

/**
 * 审批流程Service接口
 */
public interface ApprovalFlowService {
    String defaultFlowId = "-1";
    String defaultFlowName = "默认审批流";
    String defaultDescription = "默认审批流，选改审批流将直接审批通过";

    /**
     * 查询审批流程定义列表
     */
    AjaxResult selectApprovalFlowList(ApprovalFlowReq req);

    /**
     * 新增审批流程定义
     */
    AjaxResult insertApprovalFlow(ApprovalFlow info);

    /**
     * 修改审批流程定义
     */
    AjaxResult updateApprovalFlow(ApprovalFlow info);

    /**
     * 删除审批流程定义信息
     */
    AjaxResult deleteApprovalFlowById(String flowId);

    /**
     * 查询审批流程定义
     */
    AjaxResult selectApprovalFlowNodeListByFlowId(String flowId);

    /**
     * 修改审批节点
     * 全量更新审批流下的节点列表
     */
    AjaxResult updateApprovalFlowNode(List<ApprovalNode> list);

    /**
     * 查看流程的开始节点
     */
    ApprovalNode selectApprovalFlowFirstNodeByFlowId(String flowId);
}

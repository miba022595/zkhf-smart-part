package com.zkhf.epmis.platform.approval.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlow;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlowReq;
import com.zkhf.epmis.platform.approval.domain.ApprovalNode;
import com.zkhf.epmis.platform.approval.service.ApprovalFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流程Controller
 */
@RestController
@RequestMapping("/platform/approval/flow")
public class ApprovalFlowController {

    private ApprovalFlowService approvalFlowService;

    @Autowired
    public void setApprovalFlowService(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }

    /**
     * 查询审批流程定义列表
     */
    @PostMapping("/list")
    public AjaxResult flowList(@RequestBody(required = false) ApprovalFlowReq req) {
        return approvalFlowService.selectApprovalFlowList(req);
    }

    /**
     * 新增审批流程定义
     */
    @PostMapping
    public AjaxResult flowAdd(@RequestBody ApprovalFlow info) {
        return approvalFlowService.insertApprovalFlow(info);
    }

    /**
     * 修改审批流程定义
     */
    @PutMapping
    public AjaxResult flowEdit(@RequestBody ApprovalFlow info) {
        return approvalFlowService.updateApprovalFlow(info);
    }

    /**
     * 删除审批流程定义
     */
    @DeleteMapping("/{flowId}")
    public AjaxResult flowRemove(@PathVariable String flowId) {
        return approvalFlowService.deleteApprovalFlowById(flowId);
    }

    /**
     * 获取审批流程的节点列表
     */
    @GetMapping("/node/list/{flowId}")
    public AjaxResult flowNodeList(@PathVariable("flowId") String flowId) {
        return approvalFlowService.selectApprovalFlowNodeListByFlowId(flowId);
    }

    /**
     * 修改审批节点
     * 全量更新审批流下的节点列表
     */
    @PutMapping("/node")
    public AjaxResult flowNodeEdit(@RequestBody List<ApprovalNode> list) {
        return approvalFlowService.updateApprovalFlowNode(list);
    }
}

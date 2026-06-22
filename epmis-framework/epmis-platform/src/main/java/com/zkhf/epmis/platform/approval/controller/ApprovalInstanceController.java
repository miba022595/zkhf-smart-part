package com.zkhf.epmis.platform.approval.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstance;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstanceReq;
import com.zkhf.epmis.platform.approval.service.ApprovalInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 审批业务处理 Controller
 * 原始数据的状态从审批流这里进行获取，原始数据要分多个状态时，可按业务类型进行拆分
 * 先在列表查看数据，然后进行审批
 * 具体业务上，选择审批流进行审批提交，提交信息保存
 * 命中登录人时，可以点击审批，点击后，可选择消息推送人员，生成审批历史，保存
 * 可以加个人的待阅列表（消息推送的（先手动按状态查列表），点开后就变成已读）
 */
@RestController
@RequestMapping("/platform/approval/instance")
public class ApprovalInstanceController {

    private ApprovalInstanceService approvalInstanceService;
    @Autowired
    public void setApprovalProcessService(ApprovalInstanceService approvalInstanceService) {
        this.approvalInstanceService = approvalInstanceService;
    }

    /**
     * 查询审批实例
     */
    @GetMapping("/byBusiness")
    public AjaxResult selectApprovalInstanceByBusiness(@RequestParam(required = false) String businessType,
                                                       @RequestParam(required = false) String businessKey) {
        return approvalInstanceService.selectApprovalInstanceByBusiness(businessType, businessKey);
    }

    /**
     * 待审批统计列表
     */
    @GetMapping("/countProcessingStatus")
    public AjaxResult countProcessingStatus() {
        return approvalInstanceService.countProcessingStatus();
    }

    /**
     * 查询审批实例
     */
    @PostMapping("/list")
    public AjaxResult instanceList(@RequestBody(required = false) ApprovalInstanceReq req) {
        return approvalInstanceService.selectApprovalInstanceList(req);
    }

    /**
     * 获取审批历史信息
     */
    @GetMapping(value = "/historyList")
    public AjaxResult instanceHistoryList(@RequestParam String businessType, @RequestParam String businessKey) {
        return AjaxResult.success(approvalInstanceService.selectApprovalInstanceHistoryList(businessType, businessKey));
    }

    /**
     * 提交审批，新增审批实例
     */
    @PostMapping
    public AjaxResult add(@RequestBody ApprovalInstance info) {
        return approvalInstanceService.insertApprovalInstance(info);
    }

    /**
     * 相关人员进行审批工作，修改审批实例
     */
    @PutMapping
    public AjaxResult edit(@RequestBody ApprovalInstance info) {
        return approvalInstanceService.updateApprovalInstance(info);
    }

    /**
     * 查看消息的列表（可用于看待阅列表）
     */
    @GetMapping("/messageList")
    public AjaxResult messageList(@RequestParam(required = false) Integer pushStatus) {
        return approvalInstanceService.selectApprovalInstanceMessageList(pushStatus);
    }

    /**
     * 消息修改，可变更为已阅
     */
    @PutMapping("/messageNoted")
    public AjaxResult messageNoted(@RequestParam Long messageId) {
        return approvalInstanceService.approvalInstanceMessageNoted(messageId);
    }
}

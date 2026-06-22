package com.zkhf.epmis.platform.approval.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.spring.SpringUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.approval.domain.*;
import com.zkhf.epmis.platform.approval.enums.ApprovalFlowBusinessType;
import com.zkhf.epmis.platform.approval.enums.ApprovalInstanceActionType;
import com.zkhf.epmis.platform.approval.enums.ApprovalInstanceStatus;
import com.zkhf.epmis.platform.approval.service.ApprovalFlowService;
import com.zkhf.epmis.platform.approval.service.ApprovalInstanceService;
import com.zkhf.epmis.platform.approval.service.ApprovalService;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.approval.ApprovalInstanceMapper;
import com.zkhf.epmis.platform.utils.DictUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批实例：存储具体的审批实例信息Service业务层处理
 */
@Slf4j
@Service
public class ApprovalInstanceServiceImpl implements ApprovalInstanceService {

    private ApprovalInstanceMapper approvalInstanceMapper;
    @Autowired
    public void setApprovalInstanceMapper(ApprovalInstanceMapper approvalInstanceMapper) {
        this.approvalInstanceMapper = approvalInstanceMapper;
    }

    private ApprovalFlowService approvalFlowService;
    @Autowired
    public void setApprovalFlowService(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }

    @Override
    public AjaxResult selectApprovalInstanceByBusiness(String businessType, String businessKey) {
        ApprovalInstance instance = approvalInstanceMapper.selectApprovalInstanceByBusiness(businessType, businessKey);
        // 审批流程定义-业务类型，字典 approval_flow_business_type
        Map<String, DictData> bym = DictUtils.getDictCacheMap(ApprovalFlow.BUSINESS_TYPE_DICT);
        // 数据填充
        fillData(instance, GVarContainer.getUserId(), bym);
        return AjaxResult.success(instance);
    }

    @Override
    public AjaxResult countProcessingStatus() {
        List<Map<String, Object>> list = approvalInstanceMapper.countProcessingStatus(ApprovalInstanceStatus.PROCESSING.code, GVarContainer.getUserId());
        // 数据填充
        Map<String, Integer> total = new HashMap<>();
        if (!list.isEmpty()) {
            list.forEach( e -> {
                Integer num = MapUtil.getInt(e, "num");
                if (null != num) {
                    total.put(MapUtil.getStr(e, "type"), MapUtil.getInt(e, "num"));
                }
           });
        }
        List<Map<String, Object>> result = new ArrayList<>();
        // 审批流程定义-业务类型，字典 approval_flow_business_type
        Map<String, DictData> bym = DictUtils.getDictCacheMap(ApprovalFlow.BUSINESS_TYPE_DICT);
        bym.forEach( (k, v) -> {
            Map<String, Object> data = new HashMap<>();
            data.put("type", k);
            data.put("desc", v.getDictLabel());
            data.put("num", total.getOrDefault(k, 0));
            result.add(data);
        });
        return AjaxResult.success(result);
    }

    @Override
    public AjaxResult selectApprovalInstanceList(ApprovalInstanceReq req) {
        // 请求参数转换
        if (null == req) {
            req = new ApprovalInstanceReq();
        }
        // 添加权限
        Long userId = null;
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            // 审批示例中，当前审批人为登录人时，且审批状态为审批中时，可以显示审批按钮
            userId = GVarContainer.getUserId();
        }
        // 分页查询
        boolean page = PageUtils.startPageCheckExists();
        List<ApprovalInstance> list = approvalInstanceMapper.selectApprovalInstanceList(req);
        // 数据填充
        fillData(list, userId);
        return PageUtils.getAjaxResult(list, page);
    }

    private void fillData(List<ApprovalInstance> list, Long userId) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 审批流程定义-业务类型，字典 approval_flow_business_type
        Map<String, DictData> bym = DictUtils.getDictCacheMap(ApprovalFlow.BUSINESS_TYPE_DICT);
        list.forEach(e -> {
            if (ApprovalFlowService.defaultFlowId.equals(e.getFlowId())) {
                e.setFlowName(ApprovalFlowService.defaultFlowName);
            }
            fillData(e, userId, bym);
        });
    }

    private void fillData(ApprovalInstance instance, Long userId, Map<String, DictData> bym) {
        if (null == instance) {
            return;
        }
        if (bym.containsKey(instance.getBusinessType())) {
            instance.setBusinessTypeDesc(bym.get(instance.getBusinessType()).getDictLabel());
        }
        instance.setStatusDesc(ApprovalInstanceStatus.getNameByCode(instance.getStatus()));
        // 当前审批人是登录用户时，且审批状态为审批中，则设置为可审批状态
        if (null != userId && userId.equals(instance.getCurrentAssignee()) && ApprovalInstanceStatus.PROCESSING.code.equals(instance.getStatus())) {
            instance.setApproval(true);
        }
        if (StringUtils.isNotEmpty(instance.getFormDataStr())) {
            instance.setFormData(JSONObject.parseObject(instance.getFormDataStr()));
        }
    }

    @Override
    public List<ApprovalHistory> selectApprovalInstanceHistoryList(String businessType, String businessKey) {
        List<ApprovalHistory> list = approvalInstanceMapper.selectApprovalHistoryList(businessType, businessKey);
        list.forEach( e -> e.setActionTypeDesc(ApprovalInstanceActionType.getNameByCode(e.getActionType())));
        return list;
    }

    @Override
    @Log(title = "审批实例：存储具体的审批实例信息", businessType = BusinessType.INSERT)
    public AjaxResult insertApprovalInstance(ApprovalInstance info) {
        String businessTypeDesc = DictUtils.getDictLabel(ApprovalFlow.BUSINESS_TYPE_DICT, info.getBusinessType());
        if (StringUtils.isEmpty(businessTypeDesc)) {
            return AjaxResult.error("不支持的业务类型");
        }
        // 判断是否已存在相同的审批，业务主键
        int exists = approvalInstanceMapper.checkExistsForInsert(info);
        if (exists > 0) {
            return AjaxResult.error("业务已提交审批，请确认");
        }
        // 获取流程开始节点
        ApprovalNode firstNode = approvalFlowService.selectApprovalFlowFirstNodeByFlowId(info.getFlowId());
        if (firstNode == null) { // 没有开始节点，直接审批通过
            info.setStatus(ApprovalInstanceStatus.APPROVED.code);
        } else {
            info.setStatus(ApprovalInstanceStatus.PROCESSING.code);
            info.setCurrentNodeId(firstNode.getNodeId());
            info.setCurrentAssignee(firstNode.getAssigneeUser());
        }
        info.setInitiator(GVarContainer.getUserId());
        if (null != info.getFormData()) {
            info.setFormDataStr(info.getFormData().toJSONString());
        }
        int count = approvalInstanceMapper.insertApprovalInstance(info);
        if (count > 0) {
            // 添加历史信息
            approvalInstanceMapper.insertApprovalHistory(getApprovalHistory(info, ApprovalInstanceActionType.START.code));
            // 变更原有业务状态（审批中或已完成）
            approval(info);
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "审批实例：存储具体的审批实例信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateApprovalInstance(ApprovalInstance info) {
        if (info == null || StringUtils.isEmpty(info.getBusinessType()) || StringUtils.isEmpty(info.getBusinessKey())) {
            return AjaxResult.error("业务类型或业务主键不能为空");
        }
        // 审批通过时，获取下一个审批人，没有下一个则当前任务审批完成
        String actionType;
        // 获取审批的上下节点信息
        JSONObject nodeInfo = approvalInstanceMapper.selectBusinessApprovalFlowNode(info.getBusinessType(), info.getBusinessKey());
        if (null == nodeInfo) {
            return AjaxResult.error("审批节点为空，请确认");
        }
        if (!ApprovalInstanceStatus.PROCESSING.code.equals(nodeInfo.getString("status"))) {
            return AjaxResult.error("流程已审批完成，无需重复审批");
        }
        Long userId = GVarContainer.getUserId();
        if (null == userId) {
            return AjaxResult.error("未知用户");
        }
        String nodeId = nodeInfo.getString("nodeId");
        // 置空，防止参数注入错误
        info.setCurrentNodeId(null);
        info.setCurrentAssignee(null);
        if (ApprovalInstanceStatus.CANCELLED.code.equals(info.getStatus())) {
            ApprovalInstance instance = approvalInstanceMapper.selectApprovalInstanceByBusiness(info.getBusinessType(), info.getBusinessKey());
            if (instance == null) {
                return AjaxResult.error("审批实例不存在");
            }
            if (GVarContainer.isNotAdmin()) {
                Long currentAssignee = nodeInfo.getLong("currentAssignee");
                boolean isInitiator = instance.getInitiator() != null && userId.equals(instance.getInitiator());
                boolean isCurrentAssignee = userId.equals(currentAssignee);
                if (!isInitiator && !isCurrentAssignee) {
                    return AjaxResult.error("仅发起人或当前审批人可取消审批");
                }
            }
            actionType = ApprovalInstanceActionType.CANCEL.code;
        } else {
            if (!userId.equals(nodeInfo.getLong("currentAssignee"))) {
                return AjaxResult.error("无审批权限");
            }
            if (ApprovalInstanceStatus.APPROVED.code.equals(info.getStatus())) {
                String nextNodeId = nodeInfo.getString("nextNodeId");
                if (StringUtils.isNotEmpty(nextNodeId)) {
                    // 存在下一节点时，状态变为下一节点的审批中
                    info.setStatus(ApprovalInstanceStatus.PROCESSING.code);
                    info.setCurrentNodeId(nextNodeId);
                    info.setCurrentAssignee(nodeInfo.getLong("nextAssignee"));
                }
                actionType = ApprovalInstanceActionType.APPROVE.code;
            } else if (ApprovalInstanceStatus.REJECTED.code.equals(info.getStatus())) {
                actionType = ApprovalInstanceActionType.REJECT.code;
            } else {
                return AjaxResult.error("未知的审批状态");
            }
        }
        int count = approvalInstanceMapper.updateApprovalInstance(info);
        if (count > 0) {
            // 添加历史信息
            ApprovalHistory history = getApprovalHistory(info, actionType);
            history.setNodeId(nodeId);
            history.setComment(info.getComment());
            approvalInstanceMapper.insertApprovalHistory(history);
            // 添加消息推送
            if (null != info.getMessageList() && !info.getMessageList().isEmpty()) {
                info.getMessageList().forEach( e -> {
                    e.setPushStatus(0);
                    e.setPushTime(LocalDateTime.now());
                });
                approvalInstanceMapper.insertApprovalMessage(history.getId(), info.getMessageList());
            }
            // 变更原有业务状态
            approval(info);
        }
        return AjaxResult.success(count);
    }

    private void approval(ApprovalInstance info) {
        try {
            if (info == null) {
                return;
            }
            String beanName = ApprovalFlowBusinessType.getNameByCode(info.getBusinessType());
            if (StringUtils.isEmpty(beanName)) {
                return;
            }
            if (!SpringUtils.containsBean(beanName)) {
                return;
            }
            ApprovalService approval = SpringUtils.getBean(beanName);
            approval.approval(info);
        } catch (Exception e) {
            log.error("approval error", e);
        }
    }

    private ApprovalHistory getApprovalHistory(ApprovalInstance info, String actionType) {
        LoginUser user = GVarContainer.getLoginUser();
        ApprovalHistory history = new ApprovalHistory();
        history.setBusinessType(info.getBusinessType());
        history.setBusinessKey(info.getBusinessKey());
        history.setActionType(actionType);
        if (null != user) {
            history.setActionUser(user.getUserId());
            history.setIpAddress(user.getIpaddr());
            history.setActionLocation(user.getLoginLocation());
            history.setBrowser(user.getBrowser());
            history.setOs(user.getOs());
        }
        return history;
    }

    @Override
    public AjaxResult selectApprovalInstanceMessageList(Integer pushStatus) {
        Long userId = null;
        if (GVarContainer.isNotAdmin()) {
            userId = GVarContainer.getUserId();
        }
        List<ApprovalMessage> list = approvalInstanceMapper.selectApprovalMessage(userId, pushStatus);
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult approvalInstanceMessageNoted(Long messageId) {
        approvalInstanceMapper.updateApprovalMessagePushStatus(messageId, LocalDateTime.now());
        return AjaxResult.success();
    }
}

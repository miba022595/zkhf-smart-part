package com.zkhf.epmis.platform.approval.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlow;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlowReq;
import com.zkhf.epmis.platform.approval.domain.ApprovalNode;
import com.zkhf.epmis.platform.approval.service.ApprovalFlowService;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.approval.ApprovalFlowMapper;
import com.zkhf.epmis.platform.utils.DictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批流程定义Service业务层处理
 */
@Service
public class ApprovalFlowServiceImpl implements ApprovalFlowService {

    private ApprovalFlowMapper approvalFlowMapper;

    @Autowired
    public void setApprovalFlowMapper(ApprovalFlowMapper approvalFlowMapper) {
        this.approvalFlowMapper = approvalFlowMapper;
    }

    @Override
    public AjaxResult selectApprovalFlowList(ApprovalFlowReq req) {
        // 请求参数转换
        if (null == req) {
            req = new ApprovalFlowReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        boolean page = PageUtils.startPageCheckExists();
        List<ApprovalFlow> list = approvalFlowMapper.selectApprovalFlowList(req);
        // 数据填充
        fillData(list);
        if (!page) { // 不分页查询时添加全局默认审批流返回
            ApprovalFlow defaultFlow = new ApprovalFlow();
            defaultFlow.setFlowId(defaultFlowId);
            defaultFlow.setFlowName(defaultFlowName);
            defaultFlow.setDescription(defaultDescription);
            list.add(defaultFlow);
        }
        return PageUtils.getAjaxResult(list, page);
    }

    private void fillData(List<ApprovalFlow> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 审批流程定义-业务类型，字典 approval_flow_business_type
        Map<String, DictData> bym = DictUtils.getDictCacheMap(ApprovalFlow.BUSINESS_TYPE_DICT);
        list.forEach( e -> {
            if (bym.containsKey(e.getBusinessType())) {
                e.setBusinessTypeDesc(bym.get(e.getBusinessType()).getDictLabel());
            }
        });
    }

    @Override
    @Log(title = "审批流程定义", businessType = BusinessType.INSERT)
    public AjaxResult insertApprovalFlow(ApprovalFlow info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("归属企业不能为空");
        }
        if (StringUtils.isEmpty(info.getFlowName())) {
            return AjaxResult.error("流程名称不能为空");
        }
        String businessTypeDesc = DictUtils.getDictLabel(ApprovalFlow.BUSINESS_TYPE_DICT, info.getBusinessType());
        if (StringUtils.isEmpty(businessTypeDesc)) {
            return AjaxResult.error("不支持的业务类型");
        }
        // 判断是否已存在相同的流程：企业、类型、名称、版本号相同
        int exists = approvalFlowMapper.checkExistsForInsert(info);
        if (exists > 0) {
            return AjaxResult.error("流程已存在");
        }
        // 审批流不开启时不可设置为默认审批流
        if (ApprovalFlow.ACTIVE_NO.equals(info.getActive())) {
            info.setDefaultFlow(ApprovalFlow.DEFAULT_FLOW_NO);
        }
        info.setFlowId(UlidCreator.getMonotonicUlid().toString());
        int count = approvalFlowMapper.insertApprovalFlow(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "审批流程定义", businessType = BusinessType.UPDATE)
    public AjaxResult updateApprovalFlow(ApprovalFlow info) {
        if (StringUtils.isEmpty(info.getFlowName())) {
            return AjaxResult.error("流程名称不能为空");
        }
        String businessTypeDesc = DictUtils.getDictLabel(ApprovalFlow.BUSINESS_TYPE_DICT, info.getBusinessType());
        if (StringUtils.isEmpty(businessTypeDesc)) {
            return AjaxResult.error("不支持的业务类型");
        }
        // 判断是否已存在相同的流程：企业、类型、名称、版本号相同
        int exists = approvalFlowMapper.checkExistsForUpdate(info);
        if (exists > 0) {
            return AjaxResult.error("流程已存在");
        }
        // 审批流不开启时不可设置为默认审批流
        if (ApprovalFlow.ACTIVE_NO.equals(info.getActive())) {
            info.setDefaultFlow(ApprovalFlow.DEFAULT_FLOW_NO);
        }
        int count = approvalFlowMapper.updateApprovalFlow(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "审批流程定义", businessType = BusinessType.DELETE)
    public AjaxResult deleteApprovalFlowById(String flowId) {
        int count = approvalFlowMapper.deleteApprovalFlowById(flowId);
        if (count > 0) {
            approvalFlowMapper.deleteApprovalFlowNodeByFlowIds(Collections.singletonList(flowId));
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectApprovalFlowNodeListByFlowId(String flowId) {
        String startNodeId = approvalFlowMapper.selectApprovalFlowStartNodeById(flowId);
        List<ApprovalNode> list;
        if (StringUtils.isEmpty(startNodeId)) {
            list = new ArrayList<>();
        } else {
            list = approvalFlowMapper.selectApprovalFlowNodeListByFlowId(flowId);
        }
        List<ApprovalNode> orderList = new ArrayList<>();
        // 构建节点映射
        Map<String, ApprovalNode> nodeMap = list.stream().collect(Collectors.toMap(ApprovalNode::getNodeId, node -> node));
        // 按上下节点链路排序
        findNext(startNodeId, orderList, nodeMap, new ArrayList<>());
        return AjaxResult.success(orderList);
    }

    private void findNext(String nextNodeId, List<ApprovalNode> list, Map<String, ApprovalNode> nodeMap, List<String> addList) {
        if (nodeMap.containsKey(nextNodeId) && !addList.contains(nextNodeId)) {
            ApprovalNode node = nodeMap.get(nextNodeId);
            list.add(node);
            addList.add(nextNodeId);
            findNext(node.getNextNodeId(), list, nodeMap, addList);
        }
    }

    @Override
    @Log(title = "审批节点", businessType = BusinessType.UPDATE)
    public AjaxResult updateApprovalFlowNode(List<ApprovalNode> list) {
        if (null == list || list.isEmpty()) {
            return AjaxResult.error("请求参数为空");
        }
        // 先把旧节点删了，再插入新节点
        List<String> flowIds = new ArrayList<>();
        ApprovalNode node;
        Map<String, String> nodeIdMap = new HashMap<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            node = list.get(i);
            if (StringUtils.isEmpty(node.getFlowId())) {
                return AjaxResult.error("存在未知流程的节点，请确定");
            }
            if (null == node.getNodeName()) {
                node.setNodeName("--");
            }
            if (!flowIds.contains(node.getFlowId())) {
                flowIds.add(node.getFlowId());
            }
            // 更新时有可能是新增的节点，也有可能是之前的节点
            if (StringUtils.isEmpty(node.getNodeId())) {
                node.setNodeId(UlidCreator.getMonotonicUlid().toString());
            }
            // 设置关联下一节点id
            node.setNextNodeId(nodeIdMap.get(node.getFlowId()));
            nodeIdMap.put(node.getFlowId(), node.getNodeId());
        }
        if (flowIds.isEmpty()) {
            return AjaxResult.error("未知的流程信息");
        }
        // 删除旧的节点
        approvalFlowMapper.deleteApprovalFlowNodeByFlowIds(flowIds);
        // 插入新节点
        int batch = approvalFlowMapper.batchInsertApprovalFlowNode(list);
        // 设置流程的开始节点
        if(batch > 0) {
            approvalFlowMapper.updateApprovalFlowStartNode(nodeIdMap);
        }
        return AjaxResult.success(batch);
    }

    @Override
    public ApprovalNode selectApprovalFlowFirstNodeByFlowId(String flowId) {
        return approvalFlowMapper.selectApprovalFlowFirstNodeByFlowId(flowId);
    }
}

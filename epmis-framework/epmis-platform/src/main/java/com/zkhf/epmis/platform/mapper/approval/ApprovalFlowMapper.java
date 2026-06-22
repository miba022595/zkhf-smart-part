package com.zkhf.epmis.platform.mapper.approval;

import com.zkhf.epmis.platform.approval.domain.ApprovalFlow;
import com.zkhf.epmis.platform.approval.domain.ApprovalFlowReq;
import com.zkhf.epmis.platform.approval.domain.ApprovalNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 审批流程定义Mapper接口
 */
public interface ApprovalFlowMapper {

    /**
     * 查询审批流程定义列表
     */
    List<ApprovalFlow> selectApprovalFlowList(ApprovalFlowReq req);

    /**
     * 判断是否存在相同的流程-新增时
     */
    int checkExistsForInsert(ApprovalFlow info);

    /**
     * 新增审批流程定义
     */
    int insertApprovalFlow(ApprovalFlow info);

    /**
     * 判断是否存在相同的流程-修改时
     */
    int checkExistsForUpdate(ApprovalFlow info);

    /**
     * 修改审批流程定义
     */
    int updateApprovalFlow(ApprovalFlow info);

    /**
     * 删除审批流程定义
     */
    int deleteApprovalFlowById(String flowId);

    /**
     * 删除审批流程下的节点
     */
    void deleteApprovalFlowNodeByFlowIds(@Param("flowIds") List<String> flowIds);

    /**
     * 获取审批流程的开始节点
     */
    String selectApprovalFlowStartNodeById(String flowId);

    /**
     * 获取审批流程的节点列表
     */
    List<ApprovalNode> selectApprovalFlowNodeListByFlowId(String flowId);

    /**
     * 批量插入审批节点
     */
    int batchInsertApprovalFlowNode(List<ApprovalNode> list);

    /**
     * 批量更新审批流的开始节点
     */
    void updateApprovalFlowStartNode(@Param("map") Map<String, String> paramMap);

    /**
     * 查看流程的开始节点
     */
    ApprovalNode selectApprovalFlowFirstNodeByFlowId(String flowId);
}

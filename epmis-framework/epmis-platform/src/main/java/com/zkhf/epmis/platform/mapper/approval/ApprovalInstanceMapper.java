package com.zkhf.epmis.platform.mapper.approval;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.platform.approval.domain.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审批实例：存储具体的审批实例信息Mapper接口
 */
public interface ApprovalInstanceMapper {

    /**
     * 查询审批实例：存储具体审批实例信息
     */
    ApprovalInstance selectApprovalInstanceByBusiness(@Param("businessType") String businessType, @Param("businessKey") String businessKey);

    /**
     * 待审批统计列表
     */
    List<Map<String, Object>> countProcessingStatus(@Param("status") String status, @Param("userId") Long userId);

    /**
     * 查询审批实例：存储具体审批实例信息列表
     */
    List<ApprovalInstance> selectApprovalInstanceList(ApprovalInstanceReq req);

    /**
     * 查询审批历史
     */
    List<ApprovalHistory> selectApprovalHistoryList(@Param("businessType") String businessType, @Param("businessKey") String businessKey);

    /**
     * 新增审批实例：存储具体审批实例信息
     * 判断是否已经提交审批
     */
    int checkExistsForInsert(ApprovalInstance info);

    /**
     * 新增审批实例：存储具体审批实例信息
     */
    int insertApprovalInstance(ApprovalInstance info);

    /**
     * 新增审批历史：存储审批过程中的所有操作记录
     */
    void insertApprovalHistory(ApprovalHistory info);

    /**
     * 查看审批的当前节点及下一节点信息
     */
    JSONObject selectBusinessApprovalFlowNode(@Param("businessType") String businessType, @Param("businessKey") String businessKey);

    /**
     * 修改审批实例：存储具体审批实例信息
     */
    int updateApprovalInstance(ApprovalInstance info);

    /**
     * 新增审批历史：存储审批过程中的所有操作记录
     */
    void insertApprovalMessage(@Param("historyId") Long historyId, @Param("messageList") List<ApprovalMessage> messageList);

    /**
     * 查询审批消息推送：存储需要推送的消息记录，支持多人推送列表
     */
    List<ApprovalMessage> selectApprovalMessage(@Param("userId") Long userId, @Param("pushStatus") Integer pushStatus);

    /**
     * 消息修改，可变更为已阅
     */
    void updateApprovalMessagePushStatus(@Param("messageId") Long messageId, @Param("readTime") LocalDateTime readTime);
}

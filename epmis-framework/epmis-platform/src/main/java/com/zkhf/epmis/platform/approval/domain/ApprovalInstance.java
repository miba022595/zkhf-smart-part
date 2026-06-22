package com.zkhf.epmis.platform.approval.domain;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批实例：存储具体的审批实例信息对象 t_bas_approval_instance
 */
@Data
public class ApprovalInstance {

    /**
     * 业务类型：
     * 参考字典：approval_flow_business_type
     */
    private String businessType;
    private String businessTypeDesc;

    /** 业务主键：关联业务数据的唯一标识 */
    private String businessKey;
    /** 业务数据，审批时查阅 */
    @JsonIgnore
    private String formDataStr;
    private JSONObject formData;

    /** 关联企业id */
    private String entCode;
    private String entName;

    /** 流程ID，关联t_bas_approval_flow.flow_id */
    private String flowId;
    /** 流程名称 */
    private String flowName;

    /** 审批状态：PROCESSING-审批中，APPROVED-已通过，REJECTED-已拒绝，CANCELLED-已取消 */
    private String status;
    private String statusDesc;

    /** 是否可审批，当前审批人为登录人时，且审批状态为审批中时，可以显示审批按钮 */
    private boolean isApproval;

    /** 当前节点ID：当前正在审批的节点ID */
    private String currentNodeId;
    private String currentNodeName;

    /** 当前审批人：当前需要审批的用户ID */
    private Long currentAssignee;
    private String currentAssigneeName;

    /** 发起人：流程发起人的用户ID */
    private Long initiator;
    private String initiatorName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 推送的消息 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<ApprovalMessage> messageList;

    /** 操作说明：审批意见 */
    private String comment;

}

package com.zkhf.epmis.platform.approval.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批节点对象 t_bas_approval_node
 */
@Data
public class ApprovalNode {

    /** 节点主键ID */
    private String nodeId;

    /** 流程ID，关联t_bas_approval_flow.flow_id */
    private String flowId;
    private String flowName;

    /** 节点显示名称 */
    private String nodeName;

    /** 审批人ID */
    private Long assigneeUser;
    private String assigneeUserName;

    /** 下一节点ID，为空表示流程结束 */
    private String nextNodeId;

    /** 超时时间(小时)，超时提醒 */
    private Integer timeoutHours;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}

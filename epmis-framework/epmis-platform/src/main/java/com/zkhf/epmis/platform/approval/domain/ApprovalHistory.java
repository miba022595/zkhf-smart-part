package com.zkhf.epmis.platform.approval.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批历史：存储审批过程中的所有操作记录对象 t_bas_approval_history
 */
@Data
public class ApprovalHistory {

    /** 主键ID */
    private Long id;

    /** 业务类型：与流程定义中的业务类型对应 */
    private String businessType;

    /** 业务主键：关联业务数据的唯一标识 */
    private String businessKey;

    /** 操作类型：START-发起审批，APPROVE-同意，REJECT-拒绝，CANCEL-取消 */
    private String actionType;
    private String actionTypeDesc;

    /** 操作人：执行操作的用户ID */
    private Long actionUser;
    private String actionUserName;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;

    /** 操作说明：审批意见 */
    private String comment;

    /** 节点ID：操作发生时所在的节点ID */
    private String nodeId;
    private String nodeName;

    /** 登录IP地址 */
    private String ipAddress;

    /** 登录地点 */
    private String actionLocation;

    /** 浏览器类型 */
    private String browser;

    /** 操作系统 */
    private String os;

}

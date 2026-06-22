package com.zkhf.epmis.platform.approval.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台审批回调 MQTT 消息体（topic: approval_result）
 */
@Data
public class ApprovalResultMqttMessage {
    /**
     * 业务类型（如 material_apply / material_in / material_out / material_return）
     */
    private String businessType;
    /**
     * 业务主键（如 applyId / inId / outId / returnId）
     */
    private String businessKey;
    /**
     * 企业编码
     */
    private String entCode;
    /**
     * 审批状态：PROCESSING / APPROVED / REJECTED / CANCELLED
     */
    private String status;
    /**
     * 审批意见
     */
    private String comment;
    /**
     * 操作人ID（可选）
     */
    private Long actionUserId;
    /**
     * 操作人姓名（可选）
     */
    private String actionUserName;
    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;
}

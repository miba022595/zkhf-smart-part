package com.zkhf.epmis.process.material.domain;

import lombok.Data;

/**
 * 物资模块订阅 approval_result 的消息体
 */
@Data
public class MaterialApprovalResultMsg {

    /**
     * 业务类型（material_apply / material_in / material_out / material_return）
     */
    private String businessType;

    /**
     * 业务主键（applyId / inId / outId / returnId）
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
     * 操作人姓名（可选）
     */
    private String actionUserName;
}

package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资申请单对象
 */
@Data
public class MaterialApplyOrder {

    /** 申请单ID */
    private String applyId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 审批流程实例ID */
    private String flowId;

    /** 申请单号 */
    private String applyNo;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /** 申请事由 */
    private String applyReason;

    /** 申请人 */
    private String applyUser;

    /** 联系方式 */
    private String contactPhone;

    /** 期望领用时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedReceiveTime;

    /** 审核状态 */
    private Integer auditStatus;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 出库状态：0-未出库，1-部分出库，2-已出库 */
    private Integer outStatus;

    /** 归还状态：0-未归还，1-部分归还，2-已归还 */
    private Integer returnStatus;

    /** 申请总数量 */
    private Double totalQty;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 附件ID列表 */
    private List<String> annexIds;

    /** 附件列表 */
    private List<AnnexInfo> annexInfoList;

    /** 操作日志明细 */
    private List<MaterialOperateLog> operateLogList;

    /** 明细列表 */
    private List<MaterialApplyOrderItem> itemList;
}

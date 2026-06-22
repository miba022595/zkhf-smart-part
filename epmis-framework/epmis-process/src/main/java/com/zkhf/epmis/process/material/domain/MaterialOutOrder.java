package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资出库单对象
 */
@Data
public class MaterialOutOrder {

    /** 出库单ID */
    private String outId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 审批流程实例ID */
    private String flowId;

    /** 出库单号 */
    private String outNo;

    /** 出库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime;

    /** 仓库ID */
    private String warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 关联申请单ID */
    private String applyId;

    /** 关联申请单号 */
    private String applyNo;

    /** 领用人员 */
    private String receiveUser;

    /** 出库人员 */
    private String outUser;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 状态 */
    private Integer status;

    /** 库存生效状态：0-未生效，1-已生效 */
    private Integer stockEffectStatus;

    /** 出库总数量 */
    private Double totalQty;

    /** 出库总金额 */
    private Double totalAmount;

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
    private List<MaterialOutOrderItem> itemList;
}

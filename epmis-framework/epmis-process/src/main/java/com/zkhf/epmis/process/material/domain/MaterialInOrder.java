package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资入库单对象
 */
@Data
public class MaterialInOrder {

    /** 入库单ID */
    private String inId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 审批流程实例ID */
    private String flowId;

    /** 入库单号 */
    private String inNo;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inTime;

    /** 仓库ID */
    private String warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 到货单号 */
    private String arrivalNo;

    /** 采购人员 */
    private String purchaser;

    /** 入库人员 */
    private String inUser;

    /** 单据状态 */
    private Integer status;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 总数量 */
    private Double totalQty;

    /** 总金额 */
    private Double totalAmount;

    /** 库存生效状态 */
    private Integer stockEffectStatus;

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
    private List<MaterialInOrderItem> itemList;
}

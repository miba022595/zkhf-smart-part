package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资归还单对象
 */
@Data
public class MaterialReturnOrder {

    /** 归还单ID */
    private String returnId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 审批流程实例ID */
    private String flowId;

    /** 归还单号 */
    private String returnNo;

    /** 关联申请单ID */
    private String applyId;

    /** 关联出库单ID */
    private String outId;

    /** 归还入库仓库ID */
    private String warehouseId;

    /** 归还入库仓库名称 */
    private String warehouseName;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /** 归还时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /** 归还人员 */
    private String returnUser;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 物资处理人员 */
    private String handlerUser;

    /** 状态 */
    private Integer status;

    /** 库存生效状态：0-未生效，1-已生效 */
    private Integer stockEffectStatus;

    /** 归还总数量 */
    private Double totalQty;

    /** 回补库存总数量 */
    private Double stockInQty;

    /** 归还说明 */
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
    private List<MaterialReturnOrderItem> itemList;
}

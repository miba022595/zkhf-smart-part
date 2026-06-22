package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物资库存流水对象
 */
@Data
public class MaterialStockFlow {
    /** 流水ID */
    private String flowId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 仓库ID */
    private String warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 物资ID */
    private String materialId;

    /** 物资编号 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 业务类型：IN/OUT/RETURN/ADJUST/FREEZE/UNFREEZE */
    private String bizType;

    /** 业务主表ID */
    private String bizId;

    /** 业务明细ID */
    private String bizItemId;

    /** 业务单号 */
    private String bizNo;

    /** 库存变动数量（正数增加，负数减少） */
    private Double qtyChange;

    /** 变动前当前库存 */
    private Double beforeQty;

    /** 变动后当前库存 */
    private Double afterQty;

    /** 变动前可用库存 */
    private Double beforeAvailableQty;

    /** 变动后可用库存 */
    private Double afterAvailableQty;

    /** 变动前冻结库存 */
    private Double beforeFrozenQty;

    /** 变动后冻结库存 */
    private Double afterFrozenQty;

    /** 操作人 */
    private String operateBy;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;

    /** 备注 */
    private String remark;
}

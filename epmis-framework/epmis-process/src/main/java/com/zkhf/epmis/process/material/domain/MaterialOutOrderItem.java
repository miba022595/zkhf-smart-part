package com.zkhf.epmis.process.material.domain;

import lombok.Data;

/**
 * 物资出库单明细对象
 */
@Data
public class MaterialOutOrderItem {

    /** 出库单明细ID */
    private String outItemId;

    /** 出库单ID */
    private String outId;

    /** 关联申请单明细ID */
    private String applyItemId;

    /** 物资ID */
    private String materialId;

    /** 物资编号 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 品牌 */
    private String brand;

    /** 规格型号 */
    private String modelSpec;

    /** 分类名称 */
    private String categoryName;

    /** 单位 */
    private String unit;

    /** 申请数量 */
    private Double applyQty;

    /** 实际出库数量 */
    private Double outQty;

    /** 累计归还数量 */
    private Double returnedQty;

    /** 单价 */
    private Double unitPrice;

    /** 金额 */
    private Double amount;

    /** 排序号 */
    private Integer sortNum;

    /** 备注 */
    private String remark;
}

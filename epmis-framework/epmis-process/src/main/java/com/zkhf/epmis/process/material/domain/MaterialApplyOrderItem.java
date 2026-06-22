package com.zkhf.epmis.process.material.domain;

import lombok.Data;

/**
 * 物资申请单明细对象
 */
@Data
public class MaterialApplyOrderItem {

    /** 申请单明细ID */
    private String applyItemId;

    /** 申请单ID */
    private String applyId;

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

    /** 累计出库数量 */
    private Double outQty;

    /** 累计归还数量 */
    private Double returnQty;

    /** 用途说明 */
    private String purposeDesc;

    /** 排序号 */
    private Integer sortNum;

    /** 备注 */
    private String remark;
}

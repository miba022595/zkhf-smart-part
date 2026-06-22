package com.zkhf.epmis.process.material.domain;

import lombok.Data;

/**
 * 物资归还单明细对象
 */
@Data
public class MaterialReturnOrderItem {

    /** 归还单明细ID */
    private String returnItemId;

    /** 归还单ID */
    private String returnId;

    /** 关联出库单明细ID */
    private String outItemId;

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

    /** 原出库数量 */
    private Double outQty;

    /** 可归还数量 */
    private Double canReturnQty;

    /** 本次归还数量 */
    private Double returnQty;

    /** 实际回补库存数量 */
    private Double stockInQty;

    /** 处理结果 */
    private String processResult;

    /** 排序号 */
    private Integer sortNum;

    /** 备注 */
    private String remark;
}

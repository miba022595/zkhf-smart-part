package com.zkhf.epmis.process.material.domain;

import lombok.Data;

import java.util.List;

/**
 * 物资库存查询对象
 */
@Data
public class MaterialStockReq {

    /** 企业编码 */
    private String entCode;

    /** 企业编码列表 */
    private List<String> entCodes;

    /** 仓库ID */
    private String warehouseId;

    /** 物资ID */
    private String materialId;

    /** 物资名称 */
    private String materialName;

    /** 品牌 */
    private String brand;

    /** 规格型号 */
    private String modelSpec;

    /** 物资分类编码 */
    private String categoryCode;

    /** 库存状态：1-正常，2-低库存，3-无库存 */
    private Integer stockStatus;
}

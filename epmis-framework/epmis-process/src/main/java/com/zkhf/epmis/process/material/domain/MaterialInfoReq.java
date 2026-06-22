package com.zkhf.epmis.process.material.domain;

import lombok.Data;

import java.util.List;

/**
 * 物资基础信息查询对象
 */
@Data
public class MaterialInfoReq {

    /** 物资ID */
    private String materialId;

    /** 企业编码 */
    private String entCode;

    /** 企业编码列表 */
    private List<String> entCodes;

    /** 物资编号 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 品牌 */
    private String brand;

    /** 规格型号 */
    private String modelSpec;

    /** 物资分类编码 */
    private String categoryCode;

    /** 状态 */
    private Integer status;
}

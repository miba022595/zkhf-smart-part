package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物资库存对象
 */
@Data
public class MaterialStock {

    /** 库存ID */
    private String stockId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 仓库ID */
    private String warehouseId;

    /** 仓库编号 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

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

    /** 物资分类编码 */
    private String categoryCode;

    /** 物资分类名称 */
    private String categoryName;

    /** 单位 */
    private String unit;

    /** 当前库存 */
    private Double currentQty;

    /** 可用库存 */
    private Double availableQty;

    /** 冻结库存 */
    private Double frozenQty;

    /** 最低库存预警值 */
    private Double minStock;

    /** 库存状态：1-正常，2-低库存，3-无库存 */
    private Integer stockStatus;

    /** 最近变动时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastChangeTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

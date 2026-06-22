package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

@Data
public class WasteStatInfo {
    /** 固废种类ID */
    private String categoryId;
    /** 废物名称 */
    private String wasteName;
    /** 固废分类ID */
    private String wasteDictId;
    private String wasteCategory;
    /** 设计生产量(t/a) */
    private Double designOutput = 0.0;
    /** 实际产生量(t) */
    private Double actualGenerateQty = 0.0;
    /** 减量(t) */
    private Double reductionQty = 0.0;
    /** 入库量(t) */
    private Double storageQty = 0.0;
    /** 出库量(t) */
    private Double outboundQty = 0.0;
    /** 当前库存(t) */
    private Double currentInventory = 0.0;
}

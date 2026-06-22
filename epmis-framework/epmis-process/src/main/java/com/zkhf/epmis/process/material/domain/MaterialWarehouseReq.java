package com.zkhf.epmis.process.material.domain;

import lombok.Data;

import java.util.List;

/**
 * 仓库信息查询对象
 */
@Data
public class MaterialWarehouseReq {

    /** 仓库ID */
    private String warehouseId;

    /** 企业编码 */
    private String entCode;

    /** 企业编码列表 */
    private List<String> entCodes;

    /** 仓库编号 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 状态 */
    private Integer status;
}

package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

/**
 * 固废流转关联表（统一管理所有流转关系） t_waste_flow_rel
 */
@Data
public class WasteFlowRel {

    /** 流转类型：1-产生→入库 2-产生→减量 3-产生→出库 4-入库→出库 */
    private Integer flowType;

    /** 源记录id */
    private String sourceId;

    /** 目标记录id */
    private String targetId;

    /** 处理量(t) */
    private Double qty;
}
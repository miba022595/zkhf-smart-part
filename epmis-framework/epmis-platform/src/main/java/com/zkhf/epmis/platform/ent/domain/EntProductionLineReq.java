package com.zkhf.epmis.platform.ent.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业生产线信息请求
 */
@Data
public class EntProductionLineReq {

    /** 所属企业code */
    private String entCode;
    private List<String> entCodes;

    /** 企业生产车间主键id */
    private String workshopId;

    /** 生产线名称（模糊） */
    private String lineName;

    /** 状态：1-正常，2-停用 */
    private Integer status;
}

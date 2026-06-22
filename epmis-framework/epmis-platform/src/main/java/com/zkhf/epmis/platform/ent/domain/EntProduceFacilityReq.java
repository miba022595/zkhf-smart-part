package com.zkhf.epmis.platform.ent.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业生产设施/设备请求对象
 */
@Data
public class EntProduceFacilityReq {

    /** 所属企业 */
    private String entCode;
    private List<String> entCodes;

    /** 生产设施名称 */
    private String facilityName;

    /** 设备状态：1-在用，2-停用 */
    private Integer equipmentStatus;
}

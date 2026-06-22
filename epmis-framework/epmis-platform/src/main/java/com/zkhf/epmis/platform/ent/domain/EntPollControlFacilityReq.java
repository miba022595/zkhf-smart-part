package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 企业污染治理设施请求对象
 */

@Data
public class EntPollControlFacilityReq {

    /**
     * 关联企业id
     */
    private String entCode;
    private List<String> entCodes;

    private String entName;

    /**
     * 排放口类型 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 治理设施名称
     */
    private String facilityName;
}

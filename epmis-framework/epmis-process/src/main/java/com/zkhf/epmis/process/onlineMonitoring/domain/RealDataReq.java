package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

/**
 * 实时一览请求参数
 */
@Data
public class RealDataReq {

    /** 排口主键id */
    private String outPutId;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /** 企业编码 */
    private String entCode;
    private String region;

}

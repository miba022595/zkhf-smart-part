package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.zkhf.epmis.process.mqtt.domain.RealCacheData;
import lombok.Data;

/**
 * 实时一览请求参数
 */
@Data
public class RealData {

    /** 排口主键id */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 企业编码 */
    private String entCode;
    private String entName;

    /** 实时数据 */
    private RealCacheData data;

}

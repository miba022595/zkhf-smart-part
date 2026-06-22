package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

import java.util.List;

/**
 * 运维模板请求对象
 */
@Data
public class OpsTemplateReq {

    /** 关联企业 */
    private String entCode;
    private List<String> entCodes;

    /** 关联排口 */
    private String outPutId;

    /** 运维类型编码 */
    private String templateCode;
}

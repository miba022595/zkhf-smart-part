package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运维记录请求对象
 */
@Data
public class OpsRecordReq {

    /** 关联企业 */
    private String entCode;
    private String region;
    private List<String> entCodes;

    /** 排放口ID */
    private String outPutId;

    /** 运维类型编码 */
    private String templateCode;

    /** 运维人员 */
    private Long opsUserId;

    /** 运维时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordEnd;
}

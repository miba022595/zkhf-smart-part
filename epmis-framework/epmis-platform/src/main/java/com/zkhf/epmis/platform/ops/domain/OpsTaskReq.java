package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.enums.GenApprovalType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 运维任务查询信息
 */
@Data
public class OpsTaskReq {

    /** 企业编码 */
    private String entCode;
    private String region;
    private List<String> entCodes;

    /** 排放口ID */
    private String outPutId;

    /** 运维类型编码 */
    private String templateCode;

    /** 任务类型（1：自动生成的任务，2：手动生成的任务） */
    private Integer taskType;

    /** 任务状态 {@link GenApprovalType} */
    private Integer taskStatus;

    /** 执行人 */
    private Long operator;

    /** 运维任务时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskStart;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taskEnd;

}

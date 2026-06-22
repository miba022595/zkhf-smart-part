package com.zkhf.epmis.process.enforce.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执法检查记录查询对象
 */
@Data
public class EnforceRecordReq {

    /** 权限管理 */
    private String entCode;
    private String region;
    private List<String> entCodes;

    /** 排放口ID */
    private String outPutId;

    /** 运维人员ID */
    private Long opsUser;

    /** 检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkDateStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkDateEnd;

    /** 检查结果是否合格（1：合格，0：不合格/异常） */
    private Integer checkFlag;
}

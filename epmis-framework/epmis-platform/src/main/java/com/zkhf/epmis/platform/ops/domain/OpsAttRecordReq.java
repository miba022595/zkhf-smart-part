package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤打卡记录查询对象
 */
@Data
public class OpsAttRecordReq {

    /** 用户ID */
    private Long userId;

    /** 企业编码 */
    private String entCode;
    private String region;
    private List<String> entCodes;

    /** 排放口ID */
    private String outPutId;

    /** 签到时间筛选 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime punchTimeStart;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime punchTimeEnd;

    /** 运维企业编码-第三方单位 */
    private String opsUnitId;
}

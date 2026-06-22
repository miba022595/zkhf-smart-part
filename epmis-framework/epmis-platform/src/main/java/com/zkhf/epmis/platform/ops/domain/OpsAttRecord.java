package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤打卡记录对象 t_att_record
 */
@Data
public class OpsAttRecord {

    /** 记录ID */
    private String recordId;

    /** 用户ID */
    private Long userId;
    private String userName;
    private String nickName;

    /** 运维企业id-第三方单位） */
    private String opsUnitId;
    private String opsUnitName;

    /** 企业编码 */
    private String entCode;
    private String entName;

    /** 排放口ID */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 签到时间（点击按钮自动生成） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime punchTimeIn;

    /** 签到定位信息（点击按钮时获取具体位置） */
    private String punchLocationIn;

    /** 签退时间（点击按钮自动生成） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime punchTimeOut;

    /** 签退定位信息（点击按钮时获取具体位置） */
    private String punchLocationOut;

    /** 打卡时长（分钟） */
    private Long punchDuration;
    private String punchDurationDesc;

    /** 协助人 */
    private Long assistant;
    private String assistantName;
    private String assistantNick;

    /** 协助说明 */
    private String assistantRemark;

}

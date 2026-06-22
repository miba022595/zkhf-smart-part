package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打卡对象
 */
@Data
public class OpsClock {

    /** 记录ID，存在时则为签退 */
    private String recordId;

    /** 用户ID */
    private Long userId;

    /** 运维企业编码-第三方单位 */
    private String opsUnitId;

    /** 企业编码 */
    private String entCode;

    /** 排放口ID */
    private String outPutId;

    /** 打卡时间，后端生成 */
    private LocalDateTime punchTime;

    /** 签到定位信息（点击按钮时获取具体位置） */
    private String punchLocation;

}

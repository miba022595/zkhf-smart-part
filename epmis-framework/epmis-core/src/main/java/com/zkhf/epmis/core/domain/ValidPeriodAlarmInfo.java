package com.zkhf.epmis.core.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资质有效期报警数据实体类
 */
@Data
@Builder
public class ValidPeriodAlarmInfo {

    /**
     * 企业唯一编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 企业微信关联信息
     */
    private String weComMsg;

    /**
     * 资质证件类型
     */
    private Integer confType;

    /**
     * 资质证件类型描述
     */
    private String confDesc;

    /**
     * 证书主键，唯一标识用
     */
    private String itemId;

    /**
     * 证书名称
     */
    private String itemName;

    /**
     * 资质剩余过期天数
     */
    private long leftDays;

    /**
     * 资质有效期报警类型
     */
    private String alarmType;

    /**
     * 报警频率
     * 频率格式:D-天/M-月/H-小时+数字,如D10=每10天1次
     */
    private String alarmRage;

    /**
     * 资质有效期-开始时间
     */
    private LocalDate beginDate;

    /**
     * 资质有效期-结束时间
     */
    private LocalDate endDate;

    /**
     * 上次发生报警的时间
     */
    private LocalDateTime lastSendTime;
}

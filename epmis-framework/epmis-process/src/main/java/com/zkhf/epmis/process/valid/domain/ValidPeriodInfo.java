package com.zkhf.epmis.process.valid.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 企业资质有效期预警数据对象 t_valid_period_info
 */

@Data
public class ValidPeriodInfo {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 企业唯一编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

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
     * 剩余有效天数
     */
    private long leftDays;

    /**
     * 资质有效期报警类型
     */
    private String alarmType;
    private String alarmTypeDesc;

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
     * 上次发送报警的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}

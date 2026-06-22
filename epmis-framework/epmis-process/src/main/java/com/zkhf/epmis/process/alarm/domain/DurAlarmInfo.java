package com.zkhf.epmis.process.alarm.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据报警对象 t_data_out_alarm
 * 持续报警的设计方案，完成后可替换AlarmInfo类及相关信息
 * todo 可保存5年内的数据，之前的数据迁移到历史表里（后边做）
 */
@Data
public class DurAlarmInfo {

    /**
     * 主键id
     */
    private String alarmId;

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 污染因子编码
     */
    private String pollutantCode;

    /**
     * 数据类型
     * 参见 {@link DataTypeEnum}
     */
    private Integer dataType;

    /**
     * 报警类型
     * {@link AlarmDetailTypeEnum}
     */
    private Integer alarmType;

    /**
     * 报警发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 报警结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 持续时间
     */
    private String duration;

    /**
     * 报警状态，0未解除；1已解除
     */
    private Integer alarmStatus;

    /**
     * 处理状态，0未处理；1已处理
     */
    private Integer dealStatus;

    /**
     * 报警详情
     */
    private String alarmMsg;

    /**
     * 排放口编码
     */
    private String outPutCode;
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
    private String outPutTypeDesc;

    /**
     * 污染物名称
     */
    private String pollutantNameCn;
    private String pollutantNameEn;

    /**
     * 企业编码
     */
    private String entCode;
    private String entName;

    /**
     * 报警类型描述
     */
    private String alarmTypeDesc;

}
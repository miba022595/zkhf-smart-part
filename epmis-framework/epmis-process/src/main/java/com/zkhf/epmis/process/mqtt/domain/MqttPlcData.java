package com.zkhf.epmis.process.mqtt.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQTT接收的plc原始数据实体类
 */
@Data
public class MqttPlcData {

    /**
     * 数据的主键id
     */
    private String id;

    /**
     * 单元ID(PLC单元编号)
     */
    private Integer unitId;

    /**
     * 功能码(5:单线圈 6:单寄存器 15:多线圈 16:多寄存器)
     */
    private Integer fnCode;

    /**
     * 数据类型：unitId+_+fnCode
     */
    private String type;

    /**
     * 十六进制数据字符串
     */
    private String data;

    /**
     * 原始时间戳(yyyyMMddHHmmss)
     */
    private String time;

    /**
     * 上报时间（从ts转换而来）
     */
    private LocalDateTime reportTime;
}

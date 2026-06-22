package com.zkhf.epmis.process.mqtt.domain;

import com.zkhf.epmis.core.enums.DataTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订阅的数据结构
 */
@Data
public class MqttMsgInfo {

    /**
     * 设备的mn编号
     */
    private String mnNum;

    /**
     * 检测的数据类型
     */
    private String dataType;

    /**
     * 检测时间
     */
    private LocalDateTime monitorTime;

    /**
     * 检测的数据
     */
    private Map<String, Object> monitorData;

    /**
     * 数据主键id
     * yyyyMMddHHmmss+dataType
     */
    private String outId;

    /**
     * 检测的数据类型
     * 参见 {@link DataTypeEnum}
     */
    private Integer dataTypeInt;

    /**
     * 该时刻是否有报警：0：正常；1：发生报警
     */
    private Integer dataAlarm;

    /**
     * 污染信息
     */
    private String dataInfo;
}

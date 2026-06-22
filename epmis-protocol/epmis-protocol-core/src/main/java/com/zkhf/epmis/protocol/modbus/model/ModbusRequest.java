package com.zkhf.epmis.protocol.modbus.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModbusRequest {
    /** 事务ID */
    private short transId;
    /** 单元ID */
    private byte unitId;
    /** 功能码 */
    private int fnCode;
    /** 时间戳 */
    private String time;
    /** 16进制字符串数据 */
    private String data;
    /** 协议类型 */
    private String protocol;
}

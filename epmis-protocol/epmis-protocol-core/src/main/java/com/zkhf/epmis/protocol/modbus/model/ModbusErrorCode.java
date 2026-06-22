package com.zkhf.epmis.protocol.modbus.model;

public enum ModbusErrorCode {
    ILLEGAL_FUNCTION((byte)0x01, "非法功能"),
    ILLEGAL_DATA_ADDRESS((byte)0x02, "非法数据地址"),
    ILLEGAL_DATA_VALUE((byte)0x03, "非法数据值"),
    SERVER_DEVICE_FAILURE((byte)0x04, "服务器设备故障"),
    ACKNOWLEDGE((byte)0x05, "确认"),
    SERVER_DEVICE_BUSY((byte)0x06, "服务器设备繁忙"),
    MEMORY_PARITY_ERROR((byte)0x08, "内存奇偶校验错误"),
    GATEWAY_PATH_UNAVAILABLE((byte)0x0A, "网关路径不可用"),
    GATEWAY_TARGET_NO_RESPONSE((byte)0x0B, "网关目标设备无响应"),
    ;
    public final byte code;
    public final String description;

    ModbusErrorCode(byte code, String description) {
        this.code = code;
        this.description = description;
    }
}
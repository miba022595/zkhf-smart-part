package com.zkhf.epmis.protocol.modbus.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModbusResponse {
    /** 事务ID */
    private short transId;
    /** 单元ID */
    private byte unitId;
    /** 功能码 */
    private int fnCode;
    /** 16进制字符串数据 */
    private String data;
    /** 是否异常响应 */
    private boolean exception;
    /** 协议类型 */
    private String protocol;

    public static ModbusResponse success(ModbusRequest request, String data) {
        return ModbusResponse.builder()
                .transId(request.getTransId())
                .unitId(request.getUnitId())
                .fnCode(request.getFnCode())
                .data(data)
                .protocol(request.getProtocol())
                .exception(false)
                .build();
    }

    public static ModbusResponse exception(ModbusRequest request, byte exceptionCode) {
        return ModbusResponse.builder()
                .transId(request.getTransId())
                .unitId(request.getUnitId())
                .fnCode(request.getFnCode())
                .data(String.format("%02X", exceptionCode & 0xFF))
                .protocol(request.getProtocol())
                .exception(true)
                .build();
    }
}
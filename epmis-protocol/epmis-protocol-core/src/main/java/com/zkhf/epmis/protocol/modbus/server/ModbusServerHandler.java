package com.zkhf.epmis.protocol.modbus.server;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.protocol.modbus.model.FunctionCode;
import com.zkhf.epmis.protocol.modbus.model.ModbusErrorCode;
import com.zkhf.epmis.protocol.modbus.model.ModbusRequest;
import com.zkhf.epmis.protocol.modbus.model.ModbusResponse;
import com.zkhf.epmis.protocol.server.mqtt.MqttClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sharable，表示这个ChannelHandler实例可以在多个ChannelPipeline之间共享
 */
@Slf4j
@Component
@io.netty.channel.ChannelHandler.Sharable
public class ModbusServerHandler extends SimpleChannelInboundHandler<ModbusRequest> {

    @Value("${mqtt.topics.device-data-plc:device_data_plc}")
    private String topicDeviceDataPlc;

    private MqttClient mqttClient = null;
    @Autowired
    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ModbusRequest request) {
        try {
            log.debug("收到 {} 格式Modbus请求: unitId 0x{}, fnCode 0x{}, data={}",
                    request.getProtocol(), Integer.toHexString(request.getUnitId() & 0xFF),
                    Integer.toHexString(request.getFnCode()), request.getData());

            // 处理请求并生成响应
            ModbusResponse response = processRequest(request);

            // 发送响应
            ctx.writeAndFlush(response);

            log.debug("发送 {} 格式Modbus响应: unitId=0x{}, fnCode=0x{}, data={}",
                    response.getProtocol(), Integer.toHexString(response.getUnitId() & 0xFF),
                    Integer.toHexString(response.getFnCode()), response.getData());

        } catch (Exception e) {
            log.error("处理Modbus请求失败", e);

            // 返回设备故障异常
            ModbusResponse errorResponse = ModbusResponse.exception(request, ModbusErrorCode.SERVER_DEVICE_FAILURE.code);
            ctx.writeAndFlush(errorResponse);
        }
    }

    private ModbusResponse processRequest(ModbusRequest request) {
        if (FunctionCode.WRITE_SINGLE_COIL.code.equals(request.getFnCode())) {
            return handleWriteSingleCoil(request);
        } else if (FunctionCode.WRITE_MULTIPLE_COILS.code.equals(request.getFnCode())) {
            return handleWriteMultipleCoils(request);
        } else if (FunctionCode.WRITE_SINGLE_REGISTER.code.equals(request.getFnCode())) {
            return handleWriteSingleRegister(request);
        } else if (FunctionCode.WRITE_MULTIPLE_REGISTERS.code.equals(request.getFnCode())) {
            return handleWriteMultipleRegisters(request);
        } else if (FunctionCode.REPORT_SLAVE_ID.code.equals(request.getFnCode())) {
            // 报告从机ID - 返回基本状态
            log.info("设备查询从机状态: unitId=0x{}", Integer.toHexString(request.getUnitId() & 0xFF));
            // 格式: [数据长度][运行状态][设备标识]
            return ModbusResponse.success(request, "03014D42"); // 运行中 + "MB"
        } else if (FunctionCode.ENCAPSULATED_INTERFACE_TRANSPORT.code.equals(request.getFnCode())) {
            // 封装接口 - 返回设备标识
            log.info("设备查询设备标识: unitId=0x{}", Integer.toHexString(request.getUnitId() & 0xFF));
            // 返回基本设备信息
            return ModbusResponse.success(request, "0E010100034D424D"); // Modbus Server
        } else {
            // 暂未实现，返回非法功能码异常
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_FUNCTION.code);
        }
    }

    /**
     * 处理写单个线圈请求（功能码 0x05）
     * 请求格式：[起始地址(2) | 值(2)] (值只能是0x0000或0xFF00)
     * 响应格式：[地址(2) | 值(2)]（原样返回）
     */
    private ModbusResponse handleWriteSingleCoil(ModbusRequest request) {
        String data = request.getData();

        // ============ 1. 基本长度验证 ============
        if (data == null || data.length() < 8) { // 8个十六进制字符 = 4字节
            log.error("写单个线圈数据长度不足，需要8个十六进制字符，实际: {}", data == null ? "null" : data.length());
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        // ============ 2. 解析字段 ============
        String address = data.substring(0, 4);
        String value = data.substring(4, 8);

        // ============ 3. 验证数据有效性 ============
        if (!"0000".equals(value) && !"FF00".equals(value)) {
            log.error("线圈值 {} 非法，只能是0000(OFF)或FF00(ON)", value);
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        boolean coilState = "FF00".equals(value);
        log.info("写单个线圈 - 地址: {}，状态: {} ({})", address, coilState ? "ON" : "OFF", value);

        // ============ 4. 数据转发 ============
        sendData(request, data);

        // ============ 5. 构建成功响应 ============
        return ModbusResponse.success(request, data.substring(0, 8));
    }

    /**
     * 处理写多个线圈请求（功能码 0x0F）
     * 请求格式：[起始地址(2) | 线圈数量(2) | 字节计数(1) | 线圈值(⌈数量/8⌉)]
     * 响应格式：[起始地址(2) | 线圈数量(2)]
     */
    private ModbusResponse handleWriteMultipleCoils(ModbusRequest request) {
        String data = request.getData();

        // ============ 1. 基本长度验证 ============
        if (data == null || data.length() < 10) { // 地址4 + 数量4 + 字节计数2 = 10
            log.error("写多个线圈数据长度不足，需要至少10个十六进制字符，实际: {}", data == null ? "null" : data.length());
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        // ============ 2. 解析字段 ============
        String address = data.substring(0, 4);
        String quantity = data.substring(4, 8);
        String byteCount = data.substring(8, 10);

        int quantityInt = Integer.parseInt(quantity, 16);
        int byteCountInt = Integer.parseInt(byteCount, 16);

        // ============ 3. 验证数据有效性 ============
        // 3.1 验证线圈数量范围
        if (quantityInt < 1 || quantityInt > 1968) {
            log.error("线圈数量 {} 超出范围 [1, 1968]", quantityInt);
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        // 3.2 验证字节计数
        int expectedByteCount = (quantityInt + 7) / 8;
        if (byteCountInt != expectedByteCount) {
            log.error("字节数不匹配: byteCount={}, 期望={}", byteCountInt, expectedByteCount);
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        log.info("写多个线圈 - 起始地址: {}，数量: {}，字节数: {}", address, quantity, byteCount);

        // ============ 4. 数据转发 ============
        sendData(request, data);

        // ============ 5. 构建成功响应 ============
        return ModbusResponse.success(request, address + quantity);
    }

    /**
     * 处理写单个寄存器请求（功能码 0x06）
     * 请求格式：[起始地址(2) | 值(2)]
     * 响应格式：[起始地址(2) | 值(2)]（原样返回）
     */
    private ModbusResponse handleWriteSingleRegister(ModbusRequest request) {
        String data = request.getData();

        // ============ 1. 基本长度验证 ============
        if (data == null || data.length() < 8) {
            log.error("写单个寄存器数据长度不足，需要8个十六进制字符，实际: {}", data == null ? "null" : data.length());
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        // ============ 2. 解析字段 ============
        String address = data.substring(0, 4);
        String value = data.substring(4, 8);

        log.info("写单个寄存器 - 地址: {}, 值: {}", address, value);

        // ============ 3. 数据转发 ============
        sendData(request, data);

        // ============ 4. 构建成功响应 ============
        return ModbusResponse.success(request, data.substring(0, 8));
    }

    /**
     * 处理写多个寄存器请求（功能码 0x10）
     * 请求格式：[起始地址(2) | 寄存器数量(2) | 字节计数(1) | 寄存器值(N)]
     * 响应格式：[起始地址(2) | 寄存器数量(2)]
     */
    private ModbusResponse handleWriteMultipleRegisters(ModbusRequest request) {
        String data = request.getData();
        // ============ 1. 基本长度验证 ============
        if (data == null || data.length() < 10) { // 地址4 + 数量4 + 字节计数2 = 10
            log.error("写多个寄存器数据长度不足，需要至少10个十六进制字符，实际: {}", data == null ? "null" : data.length());
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }
        // ============ 2. 解析字段 ============
        String address = data.substring(0, 4);
        String quantity = data.substring(4, 8);
        String byteCount = data.substring(8, 10);

        int quantityInt = Integer.parseInt(quantity, 16);
        int byteCountInt = Integer.parseInt(byteCount, 16);

        // ============ 3. 验证数据有效性 ============
        // 3.1 验证寄存器数量范围
        if (quantityInt < 1 || quantityInt > 123) {
            log.error("寄存器数量 {} 超出范围 [1, 123]", quantityInt);
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }
        // 3.2 验证字节计数
        if (byteCountInt != quantityInt * 2) {
            log.error("字节数不匹配: byteCount={}, quantity={}, 期望={}", byteCountInt, quantityInt, quantityInt * 2);
            return ModbusResponse.exception(request, ModbusErrorCode.ILLEGAL_DATA_VALUE.code);
        }

        log.info("写多个寄存器 - 起始地址: {}，数量: {}，字节数: {}", address, quantity, byteCount);

        // ============ 4. 数据转发 ============
        sendData(request, data);

        // ============ 5. 构建成功响应 ============
        return ModbusResponse.success(request, address + quantity);
    }

    /**
     * 发送原始数据到MQTT保存
     * @param request Modbus请求
     * @param dataHex 十六进制数据字符串
     */
    private void sendData(ModbusRequest request, String dataHex) {
        try {
            if (mqttClient == null) {
                log.error("MQTT客户端未初始化");
                return;
            }
            JSONObject send = new JSONObject();
            send.put("unitId", request.getUnitId());
            send.put("fnCode", request.getFnCode());
            send.put("time", request.getTime());
            send.put("data", dataHex);
            log.debug("数据转发: {}", send.toJSONString());
            mqttClient.sendMessage(topicDeviceDataPlc, send.toJSONString());
        } catch (Exception e) {
            log.error("数据转发失败", e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel exception", cause);
        ctx.close();
    }
}
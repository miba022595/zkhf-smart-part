package com.zkhf.epmis.protocol.modbus.handler;

import com.zkhf.epmis.protocol.modbus.model.ModbusResponse;
import com.zkhf.epmis.protocol.modbus.model.ProtocolCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ModbusTCPEncoder extends MessageToByteEncoder<ModbusResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ModbusResponse response, ByteBuf out) {
        try {
            if (ProtocolCode.ASCII.name().equals(response.getProtocol())) {
                encodeAsciiFormat(response, out);
            } else {
                encodeBinaryFormat(response, out);
            }
        } catch (Exception e) {
            log.error("编码Modbus响应失败", e);
        }
    }

    /**
     * 编码为二进制格式（标准Modbus TCP）
     */
    private void encodeBinaryFormat(ModbusResponse response, ByteBuf out) {
        // 将数据从16进制字符串转换为字节数组
        byte[] dataBytes = hexStringToBytes(response.getData());

        // 构建MBAP头
        out.writeShort(response.getTransId());  // 事务标识符
        out.writeShort(0x0000);                  // 协议标识符

        // 计算长度：unitId(1) + functionCode(1) + data
        int dataLength = 2 + dataBytes.length;
        out.writeShort(dataLength);              // 长度字段

        // 单元标识符
        out.writeByte(response.getUnitId());

        // 功能码（如果是异常响应，需要设置最高位）
        int fnCode = response.getFnCode();
        if (response.isException()) {
            fnCode = fnCode | 0x80;
        }
        out.writeByte(fnCode);

        // 数据
        if (dataBytes.length > 0) {
            out.writeBytes(dataBytes);
        }
        log.debug("二进制格式编码成功: transId=0x{}, unitId=0x{}, fnCode=0x{}, data={}",
                Integer.toHexString(response.getTransId() & 0xFFFF),
                Integer.toHexString(response.getUnitId() & 0xFF),
                Integer.toHexString(response.getFnCode()),
                response.getData());
    }

    /**
     * 编码为ASCII格式
     */
    private void encodeAsciiFormat(ModbusResponse response, ByteBuf out) {
        // 将原始类型转换为16进制字符串
        String unitIdHex = String.format("%02X", response.getUnitId() & 0xFF);
        // 异常响应时，功能码要加0x80
        String fnCodeHex;
        if (response.isException()) {
            fnCodeHex = String.format("%02X", (response.getFnCode() | 0x80) & 0xFF);
        } else {
            fnCodeHex = String.format("%02X", response.getFnCode() & 0xFF);
        }
        // 构建ASCII数据部分（不包括LRC）
        StringBuilder dataPart = new StringBuilder();
        dataPart.append(unitIdHex);      // 地址
        dataPart.append(fnCodeHex);       // 功能码
        // 添加数据
        if (response.getData() != null && !response.getData().isEmpty()) {
            dataPart.append(response.getData());
        }
        // 计算LRC
        String lrc = calculateLRC(dataPart.toString());
        // 构建完整ASCII帧
        // 转换为字节输出，转换为字节输出
        String frameStr = ":" + dataPart + lrc + "\r\n";
        byte[] frameBytes = frameStr.getBytes(StandardCharsets.US_ASCII);
        log.info("ASCII格式编码成功: {}", frameStr.trim());
        out.writeBytes(frameBytes);
    }

    /**
     * 计算LRC校验
     */
    private String calculateLRC(String data) {
        int sum = 0;
        for (int i = 0; i < data.length(); i += 2) {
            String byteStr = data.substring(i, Math.min(i + 2, data.length()));
            int b = Integer.parseInt(byteStr, 16);
            sum += b;
        }
        int lrc = ((~sum) + 1) & 0xFF;
        return String.format("%02X", lrc);
    }

    /**
     * 将16进制字符串转换为字节数组
     */
    private byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
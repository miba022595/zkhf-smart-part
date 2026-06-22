package com.zkhf.epmis.protocol.modbus.handler;

import com.zkhf.epmis.protocol.common.utils.DateUtil;
import com.zkhf.epmis.protocol.modbus.model.FunctionCode;
import com.zkhf.epmis.protocol.modbus.model.ModbusRequest;
import com.zkhf.epmis.protocol.modbus.model.ProtocolCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ModbusTCPDecoder extends ByteToMessageDecoder {

    /** ASCII相关常量 ':' */
    private static final byte COLON = 0x3A;
    /** ASCII相关常量 '\r' */
    private static final byte CR = 0x0D;
    /** ASCII相关常量 '\n' */
    private static final byte LF = 0x0A;

    // 使用ThreadLocal避免缓冲区冲突
    private static final ThreadLocal<StringBuilder> ASCII_BUFFER = ThreadLocal.withInitial(() -> new StringBuilder(256));

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 检查是否有足够的数据进行协议检测
        if (in.readableBytes() < 1) {
            return;
        }

        // 保存当前读指针位置，以便回退
        in.markReaderIndex();

        try {
            // 查看第一个字节，判断是二进制还是ASCII
            int firstByte = in.getByte(in.readerIndex()) & 0xFF;

            // ASCII格式以 ':' (0x3A) 开头
            if (firstByte == COLON) {
                // ASCII格式处理
                decodeAsciiFormat(ctx, in, out);
            } else {
                // 二进制格式处理
                decodeBinaryFormat(ctx, in, out);
            }
        } catch (Exception e) {
            log.error("解码失败", e);
            in.resetReaderIndex();
        }
    }

    /**
     * 解码二进制格式（标准Modbus TCP）
     */
    private void decodeBinaryFormat(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 确保有足够的数据读取MBAP头(7字节) + 最小数据(1)
        int len = in.readableBytes();
        if (len < 8) {
            log.debug("二进制格式: 可读字节不足8, 实际{}", len);
            return;
        }
        try {
            // 保存位置以便可能的重试
            int readerIndex = in.readerIndex();
            // Modbus TCP帧格式：事务ID(2) + 协议ID(2) + 长度(2) + 单元ID(1) + 数据
            // 读取MBAP头
            short transId = in.readShort();
            short protocolId = in.readShort();
            int length = in.readUnsignedShort();
            byte unitId = in.readByte();

            // 验证协议标识符
            if (protocolId != 0) { // 0x0000表示modbus tcp协议
                log.warn("二进制格式: 无效的协议ID: {}, 期望0", protocolId);
                ctx.close(); // 协议ID无效时关闭连接
                return;
            }

            // 检查数据是否完整（length包括unitId）
            int remainingDataLength = length - 1; // 减去已读的unitId
            if (in.readableBytes() < remainingDataLength) {
                log.debug("二进制格式: 数据不完整, 需要{}字节, 实际{}",
                        remainingDataLength, in.readableBytes());
                // 重置读指针，等待更多数据
                in.readerIndex(readerIndex);
                return;
            }
            // 读取功能码
            if (remainingDataLength < 1) {
                log.warn("二进制格式: 数据长度无效: {}", length);
                ctx.close(); // 数据长度无效，关闭连接
                return;
            }
            int fnCode = in.readByte() & 0xFF;
            int dataLength = remainingDataLength - 1; // 减去功能码
            // 验证功能码范围（Modbus功能码通常在1-127之间）
            if (fnCode < 1 || fnCode > 127) {
                log.warn("二进制格式: 功能码超出范围: 0x{}", Integer.toHexString(fnCode));
                ctx.close(); // 无效的功能码，关闭连接
                return;
            }
            // 读取数据并转换为16进制字符串
            StringBuilder dataHex = new StringBuilder();
            for (int i = 0; i < dataLength; i++) {
                if (in.readableBytes() < 1) {
                    log.warn("二进制格式: 数据读取过程中字节不足");
                    ctx.close(); // 数据读取异常，关闭连接
                    return;
                }
                dataHex.append(String.format("%02X", in.readByte() & 0xFF));
            }
            log.debug("二进制格式解码成功: transId=0x{}, unitId=0x{}, fnCode=0x{}, data={}",
                    Integer.toHexString(transId & 0xFFFF),
                    Integer.toHexString(unitId & 0xFF),
                    Integer.toHexString(fnCode),
                    dataHex);
            // 验证功能码（记录警告但不阻断）
            if (!FunctionCode.containsCode(fnCode)) {
                log.warn("二进制格式: 未知的功能码: 0x{}", Integer.toHexString(fnCode));
                // 不关闭连接，允许扩展功能码
            }
            // 构建Modbus请求对象
            ModbusRequest request = ModbusRequest.builder()
                    .transId(transId)
                    .unitId(unitId)
                    .fnCode(fnCode)
                    .time(DateUtil.getCurrentDateStr())
                    .data(dataHex.toString())
                    .protocol(ProtocolCode.RTU.name())
                    .build();
            out.add(request);
        } catch (Exception e) {
            log.error("二进制格式解码失败", e);
            ctx.close(); // 发生异常时关闭连接
            in.resetReaderIndex();
        }
    }

    /**
     * 解码ASCII格式
     */
    private void decodeAsciiFormat(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 获取ThreadLocal缓冲区
        StringBuilder asciiBuffer = ASCII_BUFFER.get();
        asciiBuffer.setLength(0);
        // 保存起始位置
        int startIndex = in.readerIndex();
        try {
            // 确认第一个字节是':' - 转换为无符号整数比较
            int firstByte = in.readByte() & 0xFF;
            if (firstByte != COLON) {
                log.warn("ASCII格式: 期望起始符':'，但获取: 0x{}", String.format("%02X", firstByte));
                return;
            }
            // 查找结束符
            int crIndex = -1;
            while (in.readableBytes() > 0) {
                // 读取字节并转换为无符号整数
                int b = in.readByte() & 0xFF;
                // 首先检查是否到达帧结束符
                if (b == CR) {
                    crIndex = in.readerIndex() - 1;
                    if (in.readableBytes() > 0) {
                        int next = in.getByte(in.readerIndex()) & 0xFF;
                        if (next == LF) {
                            in.readByte(); // 消费LF
                            break;
                        } else {
                            // 格式错误：CR后面不是LF
                            log.warn("ASCII格式: CR后不是LF，获取: 0x{}", String.format("%02X", next));
                            ctx.close(); // 严重协议错误，关闭连接
                            return;
                        }
                    }
                }
                // 检查是否为有效的十六进制字符（如果不是结束符）
                if (crIndex == -1) { // 还没找到结束符
                    if (!isHexChar(b)) {
                        log.warn("ASCII格式: 包含非十六进制字符: 0x{}", String.format("%02X", b));
                        ctx.close(); // 包含无效字符，关闭连接
                        return;
                    }
                    asciiBuffer.append((char) b);
                }
            }
            if (crIndex == -1) {
                // 没有找到完整的帧，等待更多数据
                in.readerIndex(startIndex);
                return;
            }
            // 验证帧长度（最小：地址2 + 功能码2 + LRC2 = 6）
            if (asciiBuffer.length() < 6) {
                log.warn("ASCII格式: 帧长度太短: {}", asciiBuffer.length());
                ctx.close(); // 长度太短，可能是错误协议
                return;
            }
            // 处理ASCII帧
            processAsciiFrame(ctx, asciiBuffer.toString(), out);

        } catch (Exception e) {
            log.error("ASCII格式解码失败", e);
            ctx.close(); // 发生异常时关闭连接
            in.readerIndex(startIndex);
        }
    }

    /**
     * 处理ASCII帧
     */
    private void processAsciiFrame(ChannelHandlerContext ctx, String frame, List<Object> out) {
        try {
            log.debug("接收到ASCII帧: {}", frame);

            // 分离数据和LRC
            String dataPart = frame.substring(0, frame.length() - 2);
            String receivedLrc = frame.substring(frame.length() - 2);

            // 验证LRC校验
            String calculatedLrc = calculateLRC(dataPart);
            if (!calculatedLrc.equalsIgnoreCase(receivedLrc)) {
                log.warn("LRC校验失败: 计算值={}, 接收值={}, 帧={}",
                        calculatedLrc, receivedLrc, frame);
                ctx.close(); // 校验失败，可能是被破坏的数据
                return;
            }
            // 解析地址和功能码
            if (dataPart.length() < 4) {
                log.warn("ASCII帧数据部分太短: {}", dataPart);
                ctx.close(); // 数据部分太短，关闭连接
                return;
            }
            String addressHex = dataPart.substring(0, 2);
            String fnCodeHex = dataPart.substring(2, 4);
            String data = dataPart.length() > 4 ? dataPart.substring(4) : "";

            byte unitId;
            int fnCode;
            try {
                unitId = (byte) Integer.parseInt(addressHex, 16);
                fnCode = Integer.parseInt(fnCodeHex, 16);
            } catch (NumberFormatException e) {
                log.warn("ASCII帧解析失败: address={}, fnCode={}", addressHex, fnCodeHex);
                ctx.close(); // 解析失败，关闭连接
                return;
            }
            // 验证功能码范围（Modbus功能码通常在1-127之间）
            if (fnCode < 1 || fnCode > 127) {
                log.warn("ASCII格式: 功能码超出范围: 0x{}", fnCodeHex);
                ctx.close(); // 无效的功能码
                return;
            }
            log.debug("ASCII格式解码成功: unitId=0x{}, fnCode=0x{}, data={}",
                    Integer.toHexString(unitId & 0xFF),
                    Integer.toHexString(fnCode),
                    data);
            // 验证功能码（记录警告但不阻断）
            if (!FunctionCode.containsCode(fnCode)) {
                log.warn("ASCII格式: 未知的功能码: 0x{}", fnCodeHex);
                // 不关闭连接，允许扩展功能码
            }

            // 构建Modbus请求对象
            ModbusRequest request = ModbusRequest.builder()
                    .unitId(unitId)
                    .fnCode(fnCode)
                    .time(DateUtil.getCurrentDateStr())
                    .data(data)
                    .protocol(ProtocolCode.ASCII.name())
                    .build();
            out.add(request);
        } catch (Exception e) {
            log.error("解析ASCII帧失败: {}", frame, e);
            ctx.close(); // 发生异常时关闭连接
        }
    }

    /**
     * 计算LRC校验（纵向冗余校验）
     */
    private String calculateLRC(String data) {
        int sum = 0;
        // 确保data长度为偶数
        if (data.length() % 2 != 0) {
            log.warn("LRC计算: 数据长度不是偶数: {}", data);
            data = "0" + data;
        }
        for (int i = 0; i < data.length(); i += 2) {
            try {
                String byteStr = data.substring(i, i + 2);
                int b = Integer.parseInt(byteStr, 16);
                sum += b;
            } catch (NumberFormatException e) {
                log.warn("LRC计算: 无效的十六进制数据: {}",
                        data.substring(i, Math.min(i + 2, data.length())));
                sum += 0;
            }
        }
        int lrc = ((~sum) + 1) & 0xFF;
        return String.format("%02X", lrc);
    }

    /**
     * 判断是否为有效的十六进制字符
     */
    private boolean isHexChar(int b) {
        return (b >= '0' && b <= '9') ||
                (b >= 'A' && b <= 'F') ||
                (b >= 'a' && b <= 'f');
    }

    /**
     * 通道注销时的回调
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            // 清理当前线程的ThreadLocal资源
            ASCII_BUFFER.remove();
        } finally {
            super.channelInactive(ctx);
        }
    }
}
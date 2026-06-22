package com.zkhf.epmis.protocol.server.handler;

import com.zkhf.epmis.protocol.common.utils.CRC16Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * HJ/T 212 协议解码器
 * 协议结构: ## + 数据长度(4位) + 数据内容 + CRC校验(4位)
 */
@Slf4j
public class HJ212Decoder extends ByteToMessageDecoder {

    private static final String HEADER = "##";
    private static final int HEADER_LEN = 2;
    private static final int DATA_LEN_FIELD_LEN = 4;
    private static final int CRC_LEN = 4;
    private static final int MAX_DATA_LEN = 1024; // 212 协议规定数据段最大长度为1024

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 循环解析，处理一条数据中包含多个包的情况
        while (in.readableBytes() >= HEADER_LEN + DATA_LEN_FIELD_LEN) {
            in.markReaderIndex();

            // 查找头部 "##"
            int headerIndex = findHeader(in, in.readerIndex());
            if (headerIndex == -1) {
                // 未找到头部，跳过所有数据
                in.skipBytes(in.readableBytes());
                return;
            }

            // 移动到头部位置
            if (headerIndex > in.readerIndex()) {
                log.debug("丢弃头部之前的无效数据, 长度: {}", headerIndex - in.readerIndex());
                in.readerIndex(headerIndex);
            }

            // 检查剩余长度是否足够读取长度字段
            if (in.readableBytes() < HEADER_LEN + DATA_LEN_FIELD_LEN) {
                return;
            }

            // 读取长度字段（不移动指针）
            byte[] lenBytes = new byte[DATA_LEN_FIELD_LEN];
            in.getBytes(in.readerIndex() + HEADER_LEN, lenBytes);
            String lenStr = new String(lenBytes, StandardCharsets.US_ASCII);
            
            int dataLength;
            try {
                dataLength = Integer.parseInt(lenStr);
            } catch (NumberFormatException e) {
                log.warn("无效的HJ212协议长度字段: {}, 跳过当前头部并继续查找", lenStr);
                in.skipBytes(HEADER_LEN); 
                continue;
            }

            // 校验数据长度
            if (dataLength > MAX_DATA_LEN) {
                log.warn("HJ212协议数据段长度过长: {}, 丢弃该帧头部并继续查找", dataLength);
                in.skipBytes(HEADER_LEN);
                continue;
            }

            // 总帧长度 = 头部(2) + 长度(4) + 数据内容(dataLength) + CRC(4)
            int totalFrameLen = HEADER_LEN + DATA_LEN_FIELD_LEN + dataLength + CRC_LEN;

            if (in.readableBytes() < totalFrameLen) {
                // 关键点：如果当前数据不够一个完整包，检查后面是否已经出现了下一个 "##"
                // 如果在预期的包范围内发现了新的 "##"，说明当前包已经截断损坏
                int nextHeaderIndex = findHeader(in, in.readerIndex() + HEADER_LEN);
                if (nextHeaderIndex != -1 && nextHeaderIndex < in.readerIndex() + totalFrameLen) {
                    // 读取预期长度范围内的所有数据用于日志记录（不影响后续解析，使用getBytes不移动指针）
                    int actualAvailableLen = in.readableBytes();
                    int logLen = Math.min(totalFrameLen, actualAvailableLen);
                    byte[] frameBytes = new byte[logLen];
                    in.getBytes(in.readerIndex(), frameBytes);
                    String frameMsg = new String(frameBytes, StandardCharsets.US_ASCII);
                    
                    log.warn("检测到报文截断：预期长度={}, 当前可读={}, 预期范围内数据: {}", totalFrameLen, actualAvailableLen, frameMsg);
                    in.readerIndex(nextHeaderIndex);
                    continue; // 继续解析下一个包
                }

                // 确实是数据还没传完，重置索引等待更多数据
                in.resetReaderIndex();
                return;
            }

            // 准备进行 CRC 校验
            // 212 协议 CRC 校验范围是：从数据长度字段之后开始，到 && 结束（包含 &&）
            // 即：##0123DataPart&&CRC
            // 校验字符串是：DataPart&&
            
            // 读取整个包的内容用于校验
            byte[] frameBytes = new byte[totalFrameLen];
            in.getBytes(in.readerIndex(), frameBytes);
            String fullFrameStr = new String(frameBytes, StandardCharsets.US_ASCII);
            
            // 提取 CRC 部分（最后 4 位）
            String receivedCrc = fullFrameStr.substring(fullFrameStr.length() - 4);
            // 提取待校验数据部分（从第 6 位开始，到倒数第 4 位之前）
            String dataToVerify = fullFrameStr.substring(6, fullFrameStr.length() - 4);
            
            // 计算 CRC
            String calculatedCrc = CRC16Util.calcCrc16(dataToVerify);
            
            if (calculatedCrc.equalsIgnoreCase(receivedCrc)) {
                // 校验成功，读取完整帧
                ByteBuf frame = in.readRetainedSlice(totalFrameLen);
                out.add(frame);
            } else {
                log.warn("HJ212协议 CRC 校验失败: 接收值={}, 计算值={}, 丢弃该损坏包并继续查找", receivedCrc, calculatedCrc);
                in.skipBytes(HEADER_LEN); // 丢弃当前头部，继续查找
                continue;
            }

            // 跳过末尾可能的 \r\n
            skipLineDelimiters(in);
        }
    }

    private int findHeader(ByteBuf in, int start) {
        int end = in.writerIndex();
        for (int i = start; i < end - 1; i++) {
            if (in.getByte(i) == '#' && in.getByte(i + 1) == '#') {
                return i;
            }
        }
        return -1;
    }

    private void skipLineDelimiters(ByteBuf in) {
        while (in.isReadable()) {
            byte b = in.getByte(in.readerIndex());
            if (b == '\r' || b == '\n') {
                in.skipBytes(1);
            } else {
                break;
            }
        }
    }
}

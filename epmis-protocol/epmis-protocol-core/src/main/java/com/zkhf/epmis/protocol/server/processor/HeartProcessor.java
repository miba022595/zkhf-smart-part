package com.zkhf.epmis.protocol.server.processor;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备心跳handler
 */
@Slf4j
@Component
public class HeartProcessor {

    public void parse(ChannelHandlerContext ctx, String msg) {
        log.info("心跳数据: {}", msg);
        // 处理心跳数据
    }
}
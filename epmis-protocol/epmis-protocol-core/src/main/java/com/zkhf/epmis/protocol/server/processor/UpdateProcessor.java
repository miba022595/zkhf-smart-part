package com.zkhf.epmis.protocol.server.processor;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备升级处理器
 */
@Slf4j
@Component
public class UpdateProcessor {

    public void parse(ChannelHandlerContext ctx, String msg) {
        log.info("升级报文: {}", msg);
        // 处理升级报文
    }
}

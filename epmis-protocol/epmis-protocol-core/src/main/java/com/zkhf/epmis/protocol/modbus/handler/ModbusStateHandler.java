package com.zkhf.epmis.protocol.modbus.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 连接状态处理器，更新活动时间
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ModbusStateHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Modbus通道已建立: channelId={}, remote={}",
                ctx.channel().id().asShortText(),
                ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Modbus通道已关闭: channelId={}, remote={}",
                ctx.channel().id().asShortText(),
                ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Modbus通道异常: channelId={}, remote={}, error={}",
                ctx.channel().id().asShortText(),
                ctx.channel().remoteAddress(),
                cause.getMessage());
        ctx.close();
    }
}

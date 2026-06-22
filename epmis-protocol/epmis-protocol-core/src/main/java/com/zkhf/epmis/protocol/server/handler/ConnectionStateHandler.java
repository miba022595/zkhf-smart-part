package com.zkhf.epmis.protocol.server.handler;

import com.zkhf.epmis.protocol.server.context.ChannelHolder;
import com.zkhf.epmis.protocol.server.context.ConnectionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 连接状态处理器，更新活动时间
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ConnectionStateHandler extends ChannelInboundHandlerAdapter {

    private ConnectionManager connectionManager;
    @Autowired
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel {} 接入", ctx.channel().id().toString());
        connectionManager.addConnection(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionManager.removeConnection(ctx.channel());
        ChannelHolder.removeChannelHandlerContext(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 在开始处理消息时更新活动时间
        connectionManager.updateActivity(ctx.channel());
        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常 - 远程: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
        // 异常时也要移除连接
        connectionManager.removeConnection(ctx.channel());
        ChannelHolder.removeChannelHandlerContext(ctx);
    }
}

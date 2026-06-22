package com.zkhf.epmis.protocol.server.handler;

import com.zkhf.epmis.protocol.server.context.ChannelHolder;
import com.zkhf.epmis.protocol.server.context.ConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class IdleConnectionHandler extends ChannelInboundHandlerAdapter {

    private ConnectionManager connectionManager;
    @Autowired
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            if (event.state() == IdleState.ALL_IDLE) {
                // 30分钟无读/写操作
                Channel channel = ctx.channel();
                log.warn("Closing idle connection - Remote: {}, Idle timeout",
                        channel.remoteAddress());

                // 发送关闭通知（可选）
                if (channel.isActive()) {
                    ctx.writeAndFlush("IDLE_TIMEOUT").addListener(future -> {
                        cleanupIdleConnection(ctx);
                        ctx.close();
                    });
                } else {
                    cleanupIdleConnection(ctx);
                    ctx.close();
                }
                // 关闭连接
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private void cleanupIdleConnection(ChannelHandlerContext ctx) {
        try {
            connectionManager.removeConnection(ctx.channel());
            ChannelHolder.removeChannelHandlerContext(ctx);
        } catch (Exception e) {
            log.warn("Error during idle connection cleanup", e);
        }
    }
}

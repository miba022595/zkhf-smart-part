package com.zkhf.epmis.protocol.server.handler;

import com.zkhf.epmis.protocol.server.context.ConnectionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发送数据时更新活动时间
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class OutboundActivityHandler extends ChannelOutboundHandlerAdapter {

    private ConnectionManager connectionManager;

    @Autowired
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 发送数据时也更新活动时间
        connectionManager.updateActivity(ctx.channel());
        log.debug("Data sent, activity updated - Channel: {}", ctx.channel().id());

        super.write(ctx, msg, promise);
    }
}

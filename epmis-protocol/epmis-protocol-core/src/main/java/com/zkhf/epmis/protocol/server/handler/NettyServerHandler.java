package com.zkhf.epmis.protocol.server.handler;

import com.zkhf.epmis.protocol.server.processor.HJ212Processor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private HJ212Processor hj212Processor;
    @Autowired
    public void setHJ212Processor(HJ212Processor hj212Processor) {
        this.hj212Processor = hj212Processor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String message = (String) msg;

            if (message.startsWith("##")) {
                log.info("212协议 message: {}", message);
                hj212Processor.parse(ctx, message);
            }

        } catch (Exception e) {
            log.error("Error processing message", e);
            ctx.writeAndFlush("ERROR: " + e.getMessage());
        }
    }

}

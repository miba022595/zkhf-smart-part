package com.zkhf.epmis.protocol.server.stater;

import com.zkhf.epmis.protocol.server.context.ConnectionManager;
import com.zkhf.epmis.protocol.server.handler.ConnectionStateHandler;
import com.zkhf.epmis.protocol.server.handler.IdleConnectionHandler;
import com.zkhf.epmis.protocol.server.handler.HJ212Decoder;
import com.zkhf.epmis.protocol.server.handler.NettyServerHandler;
import com.zkhf.epmis.protocol.server.handler.OutboundActivityHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * HJ 212 netty处理
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "netty.server.enabled", havingValue = "true")
public class NettyServerStart {

    @Value("${netty.server.port:8080}")
    private int port;

    @Value("${netty.server.bossThreads:1}")
    private int bossThreads;

    @Value("${netty.server.workerThreads:0}")
    private int workerThreads;

    // 设置15分钟无数据交互关闭连接
    @Value("${netty.server.timeout.all-idle-time:1800}")
    private int allIdleTime;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private NettyServerHandler serverHandler;
    @Autowired
    public void setNettyServerHandler(NettyServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    private ConnectionManager connectionManager;
    @Autowired
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private ConnectionStateHandler connectionStateHandler;
    @Autowired
    public void setConnectionStateHandler(ConnectionStateHandler connectionStateHandler) {
        this.connectionStateHandler = connectionStateHandler;
    }

    private IdleConnectionHandler idleConnectionHandler;
    @Autowired
    public void setIdleConnectionHandler(IdleConnectionHandler idleConnectionHandler) {
        this.idleConnectionHandler = idleConnectionHandler;
    }

    private OutboundActivityHandler outboundActivityHandler;
    @Autowired
    public void setOutboundActivityHandler(OutboundActivityHandler outboundActivityHandler) {
        this.outboundActivityHandler = outboundActivityHandler;
    }

    private Channel channel;

    @PostConstruct
    public void start() throws Exception {
        try {
            // 处理workerThreads为0的情况，使用默认值
            int actualWorkerThreads = workerThreads > 0 ? workerThreads : Runtime.getRuntime().availableProcessors() * 2;
            bossGroup = new NioEventLoopGroup(bossThreads);
            workerGroup = new NioEventLoopGroup(actualWorkerThreads);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 1. 连接状态管理
                            pipeline.addLast("connectionState", connectionStateHandler);
                            // 2. 空闲检测（顺序重要：先检测，后处理）
                            pipeline.addLast("idleState", new IdleStateHandler(0, 0, allIdleTime, TimeUnit.MINUTES));
                            pipeline.addLast("idleHandler", idleConnectionHandler);
                            // 3. 编解码器（正确顺序）
                            // 3.1 HJ212协议解码（按长度字段拆分，兼容粘包/坏包跳过）
                            pipeline.addLast("hj212Decoder", new HJ212Decoder());
                            // 3.2 字节到字符串解码（入站）
                            pipeline.addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                            // 3.3 字符串到字节编码（出站）
                            pipeline.addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));
                            // 4. 发送数据活动跟踪（出站处理器）
                            pipeline.addLast("outboundActivity", outboundActivityHandler);
                            // 5. 业务处理（放在最后）
                            pipeline.addLast("businessHandler", serverHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future  = bootstrap.bind(port).sync();
            channel = future.channel();
            log.info("Netty server started on port: {}", port);
            // 添加关闭监听器
            channel.closeFuture().addListener(f -> log.info("Netty server channel closed"));
        } catch (Exception e) {
            stop();
            throw new RuntimeException("Failed to start Netty server on port: " + port, e);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            log.info("Shutting down Netty server...");
            // 第一步：清理所有连接
            int cleanedCount = connectionManager.cleanupAllConnections();

            // 第二步：关闭服务器通道
            if (channel != null) {
                channel.close().syncUninterruptibly();
            }

            // 第三步：关闭线程组
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
            log.info("Netty服务器关闭完成 - 清理连接: {}个", cleanedCount);
        } catch (Exception e) {
            log.info("Error while shutting down Netty server", e);
        }
    }
}

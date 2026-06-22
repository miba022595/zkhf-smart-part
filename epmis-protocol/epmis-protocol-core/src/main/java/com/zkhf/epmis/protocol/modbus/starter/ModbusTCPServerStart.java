package com.zkhf.epmis.protocol.modbus.starter;

import com.zkhf.epmis.protocol.modbus.handler.ModbusStateHandler;
import com.zkhf.epmis.protocol.modbus.server.ModbusServerHandler;
import com.zkhf.epmis.protocol.modbus.handler.ModbusTCPDecoder;
import com.zkhf.epmis.protocol.modbus.handler.ModbusTCPEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Slf4j
@Component
@ConditionalOnProperty(name = "modbus.tcp.enabled", havingValue = "true")
public class ModbusTCPServerStart {

    @Value("${modbus.tcp.port:502}")
    private int port;
    @Value("${modbus.tcp.bossThreads:1}")
    private int bossThreads;
    @Value("${modbus.tcp.workerThreads:0}")
    private int workerThreads;
    @Value("${modbus.tcp.soBacklog:1024}")
    private int soBacklog;
    @Value("${modbus.tcp.nodeLay:true}")
    private boolean nodeLay;
    @Value("${modbus.tcp.keepalive:true}")
    private boolean keepalive;

    private ModbusServerHandler serverHandler;
    @Autowired
    public void setModbusServerHandler(ModbusServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    private ModbusStateHandler modbusStateHandler;
    @Autowired
    public void setModbusStateHandler(ModbusStateHandler modbusStateHandler) {
        this.modbusStateHandler = modbusStateHandler;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture serverChannel;

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
                    .option(ChannelOption.SO_BACKLOG, soBacklog)
                    .childOption(ChannelOption.TCP_NODELAY, nodeLay)
                    .childOption(ChannelOption.SO_KEEPALIVE, keepalive)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 1. 连接状态管理
                            pipeline.addLast("connectionState", modbusStateHandler);
                            // 2. 通用解码器（自动识别二进制/ASCII）
                            pipeline.addLast(new ModbusTCPDecoder());
                            // 3. 通用编码器（根据请求格式自动选择响应格式）
                            pipeline.addLast(new ModbusTCPEncoder());
                            // 4. 业务处理器
                            pipeline.addLast(serverHandler);
                        }
                    });

            serverChannel = bootstrap.bind(port).sync();

            log.info("==================================================");
            log.info("Modbus TCP Server started on port {}", port);
            log.info("Auto-detect binary/ASCII protocol");
            log.info("Boss threads: {}", bossThreads);
            log.info("Worker threads: {}", actualWorkerThreads);
            log.info("==================================================");

            // 添加关闭监听器
            serverChannel.channel().closeFuture().addListener(future -> log.info("Modbus TCP Server channel closed"));

        } catch (Exception e) {
            log.error("Failed to start Modbus TCP Server", e);
            stop();
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (serverChannel != null) {
                serverChannel.channel().close().sync();
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
            }

            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
            }

            log.info("Modbus TCP Server stopped");
        } catch (InterruptedException e) {
            log.error("Error stopping Modbus TCP Server", e);
            Thread.currentThread().interrupt();
        }
    }
}

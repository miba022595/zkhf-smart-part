package com.zkhf.epmis.protocol.server.context;

import com.zkhf.epmis.protocol.base.ConnectionInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ConnectionManager {

    // 设置15分钟无数据交互关闭连接
    @Value("${netty.server.timeout.all-idle-time:15}")
    private int allIdleTime;

    // 存储连接及其最后活动时间
    private final ConcurrentMap<ChannelId, ConnectionInfo> connections =
            new ConcurrentHashMap<>();

    @Scheduled(initialDelay = 0, fixedRate = 600000) // 启动后开始执行，之后每10分钟执行
    public void init() {
        // 自动任务校验时间为配置的时间+10分钟
        int idleTimeoutMs = Math.max(15, allIdleTime) + 10;

        int closedCount = 0;

        List<ChannelId> toRemove = new ArrayList<>();

        // 第一步：识别要清理的连接
        for (ConnectionInfo info : connections.values()) {
            long idleTime = info.getIdleMinutes();
            if (idleTime > idleTimeoutMs) {
                toRemove.add(info.getChannel().id());
                log.warn("Cleaning up idle connection - Channel: {}, Idle: {} minutes",
                        info.getChannel().id(), idleTime);
            }
        }

        // 第二步：逐个清理
        for (ChannelId channelId : toRemove) {
            ConnectionInfo info = connections.get(channelId);
            if (null == info) {
                continue;
            }
            try {
                // 尝试关闭连接
                if (info.getChannel().isActive()) {
                    info.getChannel().close().await(5, TimeUnit.SECONDS);
                    closedCount++;
                }
            } catch (Exception e) {
                log.error("关闭连接异常 - Channel: {}", channelId, e);
            } finally {
                // 确保无论如何都从Map中移除
                connections.remove(channelId);
                log.info("连接已从管理器移除 - Channel: {}", channelId);
            }
        }
        if (closedCount > 0) {
            log.info("Cleaned up {} idle connections", closedCount);
        }
    }

    public void addConnection(Channel channel) {
        connections.put(channel.id(), ConnectionInfo.builder()
                .channel(channel)
                .connectTime(LocalDateTime.now())
                .activeTime(LocalDateTime.now())
                .build());

        log.info("Connection established: {} - Remote: {}",
                channel.id(), channel.remoteAddress());
    }

    public void updateActivity(Channel channel) {
        ConnectionInfo info = connections.get(channel.id());
        if (info != null) {
            info.updateActive();
        }
    }

    public void removeConnection(Channel channel) {
        ConnectionInfo removed = connections.remove(channel.id());
        if (removed != null) {
            log.info("Connection closed: {} - Duration: {}s",
                    channel.id(), removed.getTotalDurationSeconds());
        }
    }

    /**
     * 获取连接统计信息（包含空闲时间）
     */
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", connections.size());

        List<Map<String, Object>> connList = new ArrayList<>();

        for (ConnectionInfo info : connections.values()) {
            Map<String, Object> connInfo = new HashMap<>();
            Channel channel = info.getChannel();

            connInfo.put("channelId", channel.id().toString());
            connInfo.put("remoteAddress", channel.remoteAddress());
            connInfo.put("active", channel.isActive());
            connInfo.put("connectTime", info.getConnectTime());
            connInfo.put("activeTime", info.getActiveTime());

            connInfo.put("idleSeconds", info.getIdleSeconds());
            connInfo.put("totalDurationSeconds", info.getTotalDurationSeconds());

            connList.add(connInfo);
        }

        stats.put("connections", connList);
        return stats;
    }

    public Collection<ConnectionInfo> getConnections() {
        return connections.values();
    }

    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * 强制清理所有连接（服务器关闭时使用）
     */
    public int cleanupAllConnections() {
        int totalCount = connections.size();
        log.info("开始清理所有连接 - 总数: {}", totalCount);

        for (ConnectionInfo info : connections.values()) {
            try {
                if (info.getChannel().isActive()) {
                    info.getChannel().close().syncUninterruptibly();
                }
            } catch (Exception e) {
                log.error("关闭连接失败 - Channel: {}", info.getChannel().id(), e);
            }
        }

        // 清空整个Map
        int removedCount = connections.size();
        connections.clear();

        log.info("所有连接清理完成 - 移除: {}个", removedCount);
        return removedCount;
    }
}

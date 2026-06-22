package com.zkhf.epmis.protocol.base;

import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
public class ConnectionInfo {
    private Channel channel;
    private LocalDateTime activeTime;
    private LocalDateTime connectTime;

    public void updateActive() {
        this.activeTime = LocalDateTime.now();
    }

    /**
     * 获取空闲秒数
     */
    public long getIdleSeconds() {
        return Duration.between(activeTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 获取空闲分钟数
     */
    public long getIdleMinutes() {
        return Duration.between(activeTime, LocalDateTime.now()).toMinutes();
    }

    /**
     * 获取总连接时长（秒）
     */
    public long getTotalDurationSeconds() {
        return Duration.between(connectTime, LocalDateTime.now()).getSeconds();
    }
}

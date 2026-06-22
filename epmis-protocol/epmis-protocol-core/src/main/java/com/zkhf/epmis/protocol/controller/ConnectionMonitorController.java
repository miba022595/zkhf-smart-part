package com.zkhf.epmis.protocol.controller;

import com.zkhf.epmis.protocol.base.ConnectionInfo;
import com.zkhf.epmis.protocol.server.context.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/protocol/connections")
public class ConnectionMonitorController {

    private ConnectionManager connectionManager;
    @Autowired
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * 获取所有连接信息（包含空闲时间）
     */
    @GetMapping("connections")
    public Map<String, Object> getConnections() {
        return connectionManager.getConnectionStats();
    }

    /**
     * 获取空闲连接列表（超过指定分钟数）
     */
    @GetMapping("/idle/{minutes}")
    public List<Map<String, Object>> getIdleConnections(@PathVariable int minutes) {

        List<Map<String, Object>> idleConnections = new ArrayList<>();

        for (ConnectionInfo info : connectionManager.getConnections()) {
            long idleTime = info.getIdleMinutes();
            if (idleTime > minutes) {
                Map<String, Object> connInfo = new HashMap<>();
                connInfo.put("channelId", info.getChannel().id().toString());
                connInfo.put("remoteAddress", info.getChannel().remoteAddress());
                connInfo.put("idleMinutes", idleTime / (60 * 1000));
                connInfo.put("activeTime", info.getActiveTime());

                idleConnections.add(connInfo);
            }
        }

        return idleConnections;
    }

    /**
     * 手动关闭空闲连接
     */
    @PostMapping("/close-idle")
    public Map<String, Object> closeIdleConnections(
            @RequestParam(defaultValue = "30") int maxIdleMinutes) {

        int closedCount = 0;

        for (ConnectionInfo info : connectionManager.getConnections()) {
            long idleTime = info.getIdleMinutes();
            if (idleTime > maxIdleMinutes && info.getChannel().isActive()) {
                info.getChannel().close();
                closedCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("closedCount", closedCount);
        result.put("maxIdleMinutes", maxIdleMinutes);
        result.put("remainingConnections", connectionManager.getConnectionCount());

        return result;
    }
}

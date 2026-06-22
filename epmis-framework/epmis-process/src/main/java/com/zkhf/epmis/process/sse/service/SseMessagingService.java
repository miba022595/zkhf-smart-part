package com.zkhf.epmis.process.sse.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseMessagingService {

    /**
     * 客户端订阅
     */
    SseEmitter subscribe(String eventType, String clientId, String otherParam);

    /**
     * 异步广播消息给所有客户端
     */
    void notifyBroadcast(String eventType, String message);

    /**
     * 异步广播消息给所有客户端
     */
    void notifyBroadcast(String eventType, String entCode, String message);

    /**
     * 异步广播报警消息给所有客户端
     */
    void notifyBroadcastAlarmInfo(SseOnlineAlarm message);

    /**
     * 取消特定事件类型的订阅
     */
    AjaxResult unsubscribe(String eventType, String clientId);

    /**
     * 获取客户端订阅状态
     */
    AjaxResult getSubscriptionStatus(String clientId);

    /**
     * 每5分钟清理一次失效连接
     */
    void cleanupInactiveConnection();
}

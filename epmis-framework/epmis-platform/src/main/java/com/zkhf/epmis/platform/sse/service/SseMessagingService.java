package com.zkhf.epmis.platform.sse.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseMessagingService {

    /**
     * 客户端订阅
     */
//    SseEmitter subscribe(String clientId, HttpServletRequest request);
    SseEmitter subscribe(String clientId);

    /**
     * 异步向特定客户端发送消息
     */
    void notifySendToClient(String clientId, String message);

    /**
     * 异步广播消息给所有客户端
     */
    void notifyBroadcast(String message);

    /**
     * 向特定客户端发送消息
     */
    void sendToClient(String clientId, String message);

    /**
     * 广播消息给所有客户端
     */
    void broadcast(String message);
}

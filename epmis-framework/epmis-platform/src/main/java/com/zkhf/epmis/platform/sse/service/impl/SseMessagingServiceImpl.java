package com.zkhf.epmis.platform.sse.service.impl;

import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.sse.service.SseMessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseMessagingServiceImpl implements SseMessagingService {

    // 保存所有连接的客户端
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    // 记录连接客户端的信息
    private final Map<String, List<String>> entCodeMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String clientId) {
        // 设置0表示不超时，或者设置合理的超时时间(毫秒)
        SseEmitter emitter = new SseEmitter(0L);

        // 将新的emitter添加到map中
        emitters.put(clientId, emitter);
        entCodeMap.put(clientId, GVarContainer.getEntCodes());

        // 设置回调，当客户端断开时从map中移除
        emitter.onCompletion(() -> {
            log.error("Client onCompletion: " + clientId);
            emitters.remove(clientId);
            entCodeMap.remove(clientId);
        });

        emitter.onTimeout(() -> {
            log.error("Client onTimeout: " + clientId);
            emitter.complete();
            emitters.remove(clientId);
            entCodeMap.remove(clientId);
        });

        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("连接成功 " + clientId)
                    .reconnectTime(5000));
        } catch (IOException e) {
            log.error("Client completeWithError {}", clientId, e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Async
    public void notifySendToClient(String clientId, String message) {
        sendToClient(clientId, message);
    }

    @Async
    public void notifyBroadcast(String message) {
        broadcast(message);
    }

    @Override
    public void sendToClient(String clientId, String message) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("message")
                        .data(message));
            } catch (IOException e) {
                log.error("Client sendToClient {}", clientId, e);
                emitter.complete();
                emitters.remove(clientId);
                entCodeMap.remove(clientId);
            }
        }
    }

    @Override
    public void broadcast(String message) {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name("broadcast")
                        .data(message));
            } catch (IOException e) {
                log.error("Client broadcast IOException {}", clientId, e);
                emitter.complete();
                emitters.remove(clientId);
                entCodeMap.remove(clientId);
            }
        });
    }
}

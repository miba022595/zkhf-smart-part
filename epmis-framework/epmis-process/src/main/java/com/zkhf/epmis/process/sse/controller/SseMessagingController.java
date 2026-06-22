package com.zkhf.epmis.process.sse.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/process/message/subscribe")
public class SseMessagingController {

    private SseMessagingService sseMessagingService;

    @Autowired
    public void setSseMessagingService(SseMessagingService sseMessagingService) {
        this.sseMessagingService = sseMessagingService;
    }

    /**
     * 客户端订阅
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String eventType,
                                @RequestParam String clientId,
                                @RequestParam String otherParam) {
        return sseMessagingService.subscribe(eventType, clientId, otherParam);
    }

    /**
     * 取消订阅事件
     * eventType为空时取消对应客户端所有的
     */
    @GetMapping("/unsubscribe")
    public AjaxResult unsubscribe(@RequestParam(required = false) String eventType, @RequestParam String clientId) {
        return sseMessagingService.unsubscribe(eventType, clientId);
    }

    /**
     * 获取订阅状态
     */
    @GetMapping("/status")
    public AjaxResult getStatus(@RequestParam String clientId) {
        return sseMessagingService.getSubscriptionStatus(clientId);
    }

    /**
     * 每5分钟清理一次失效连接
     */
    @Scheduled(initialDelay = 10000, fixedRate = 5 * 60 * 1000)
    public void cleanupInactiveConnections() {
        sseMessagingService.cleanupInactiveConnection();
    }
}

package com.zkhf.epmis.platform.sse.controller;

import com.zkhf.epmis.platform.sse.service.SseMessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * todo 准备加个消息推送的，启动后定时检测数据，比如
 * 1. 环保投入中的部门和人员的对应关系是否发生变化，变化后，记录消息，推送到前端提示
 */
@RestController
@RequestMapping("/platform/message/subscribe")
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
    public SseEmitter subscribe(@RequestParam String clientId) {
        return sseMessagingService.subscribe(clientId);
    }
}

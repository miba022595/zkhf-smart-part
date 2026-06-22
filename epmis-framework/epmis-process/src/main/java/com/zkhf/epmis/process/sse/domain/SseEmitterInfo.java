package com.zkhf.epmis.process.sse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class SseEmitterInfo {

    public static final String Admin_ent = "-1";
    public static final List<String> Admin_ent_list = Collections.singletonList(Admin_ent);

    /**
     * 订阅的时间类型
     * 参见 {@link SubscribeEnum}
     */
    private String eventType;

    /**
     * 订阅的客户端id
     */
    private String clientId;

    /**
     * 订阅的其他参数
     * 如报警时，为订阅的报警code列表
     */
    private String otherParam;

    /**
     * 订阅的权限列表
     */
    private List<String> authList;

    /**
     * 订阅的操作客户端
     */
    @JsonIgnore
    private SseEmitter sseEmitter;

    /**
     * 保存上次发送数据时间，用于清理过期连接
     */
    private long lastSendTime;
}

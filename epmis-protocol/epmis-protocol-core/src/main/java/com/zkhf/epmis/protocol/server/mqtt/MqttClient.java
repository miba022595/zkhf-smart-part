package com.zkhf.epmis.protocol.server.mqtt;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MqttClient {

    private final MessageChannel mqttOutboundChannel;

    public MqttClient(MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    // 发送消息到指定主题
    @Async
    public void sendMessage(String topic, String payload) {
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic) // 设置消息头指定主题
                .build();
        mqttOutboundChannel.send(message);
    }

    // 发送消息到指定主题并设置QoS
    @Async
    public void sendMessage(String topic, String payload, int qos) {
        Message<String> message = MessageBuilder.withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .setHeader("mqtt_qos", qos) // 设置消息质量
                .build();
        mqttOutboundChannel.send(message);
    }
}

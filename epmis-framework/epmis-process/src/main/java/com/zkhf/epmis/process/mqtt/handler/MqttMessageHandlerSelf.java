package com.zkhf.epmis.process.mqtt.handler;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.process.material.service.MaterialApprovalMqttService;
import com.zkhf.epmis.process.mqtt.config.MqttProp;
import com.zkhf.epmis.process.mqtt.service.MqttMessageService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttMessageHandlerSelf implements MessageHandler {

    private MqttProp mqttProp;
    @Autowired
    public void setMqttProp(MqttProp mqttProp) {
        this.mqttProp = mqttProp;
    }

    private MqttMessageService mqttMessageService;
    @Autowired
    public void setMqttMessageService(MqttMessageService mqttMessageService) {
        this.mqttMessageService = mqttMessageService;
    }

    private MaterialApprovalMqttService materialApprovalMqttService;
    @Autowired
    public void setMaterialApprovalMqttService(MaterialApprovalMqttService materialApprovalMqttService) {
        this.materialApprovalMqttService = materialApprovalMqttService;
    }

    @Override
    public void handleMessage(@Nullable Message<?> message) throws MessagingException {
        if (null == message) { // 空消息不处理
            return;
        }
        // 为每个消息生成唯一标识
        String messageId = UlidCreator.getUlid().toString();
        try {
            String topic = getTopic(message);
            String payload = message.getPayload().toString();

            log.info("收到MQTT消息 messageId {} topic {}", messageId, topic);
            log.debug("收到MQTT消息 messageId {} topic {}, payload {}", messageId, topic, payload);

            // 根据主题进行特定处理
            processByTopic(messageId, topic, payload);

        } catch (Exception e) {
            log.error("处理MQTT消息时发生错误: messageId {}", messageId, e);
        }
    }

    private String getTopic(Message<?> message) {
        Object value = message.getHeaders().get("mqtt_receivedTopic");
        return value != null ? value.toString() : "unknown";
    }

    private void processByTopic(String messageId, String topic, String payload) {
        if (topic == null) {
            return;
        }
        // 根据主题模式进行路由处理
        if (mqttProp.getTopicOutPutDataReal().equals(topic)) {
            mqttMessageService.handleRealData(messageId, payload);
        } else if (mqttProp.getTopicOutPutDataMinute().equals(topic)) {
            mqttMessageService.handleMinuteData(messageId, payload);
        } else if (mqttProp.getTopicOutPutDataHour().equals(topic)) {
            mqttMessageService.handleHourData(messageId, payload);
        } else if (mqttProp.getTopicOutPutDataDay().equals(topic)) {
            mqttMessageService.handleDayData(messageId, payload);
        } else if (mqttProp.getTopicDeviceDataPlc().equals(topic)) {
            mqttMessageService.handlePlcData(messageId, payload);
        } else if (mqttProp.getTopicApprovalResult().equals(topic)) {
            materialApprovalMqttService.handleApprovalResult(messageId, payload);
        } else {
            log.error("不支持的 messageId {} topic {}", messageId, topic);
        }
    }
}

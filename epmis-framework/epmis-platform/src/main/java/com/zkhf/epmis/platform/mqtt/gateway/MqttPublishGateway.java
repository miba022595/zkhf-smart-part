package com.zkhf.epmis.platform.mqtt.gateway;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttPublishGateway {
    void publish(String payload, @Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) Integer qos);
}


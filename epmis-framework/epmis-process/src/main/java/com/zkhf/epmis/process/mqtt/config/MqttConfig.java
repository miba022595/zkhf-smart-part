package com.zkhf.epmis.process.mqtt.config;

import com.zkhf.epmis.process.mqtt.handler.MqttMessageHandlerSelf;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Data
@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MqttConfig {

    private MqttProp mqttProp;
    @Autowired
    public void setMqttProp(MqttProp mqttProp) {
        this.mqttProp = mqttProp;
    }

    private MqttMessageHandlerSelf mqttMessageHandlerSelf;
    @Autowired
    public void setMqttMessageHandlerSelf(MqttMessageHandlerSelf mqttMessageHandlerSelf) {
        this.mqttMessageHandlerSelf = mqttMessageHandlerSelf;
    }

    // 创建 MQTT 客户端工厂
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{mqttProp.getBrokerUrl()});
        if (mqttProp.getUsername() != null && !mqttProp.getUsername().isEmpty()) {
            options.setUserName(mqttProp.getUsername());
        }
        if (mqttProp.getPassword() != null && !mqttProp.getPassword().isEmpty()) {
            options.setPassword(mqttProp.getPassword().toCharArray());
        }
        options.setConnectionTimeout(mqttProp.getConnectionTimeout());
        options.setKeepAliveInterval(mqttProp.getKeepAliveInterval());
        options.setCleanSession(mqttProp.isCleanSession());
        options.setAutomaticReconnect(true);

        factory.setConnectionOptions(options);
        return factory;
    }

    // 创建接收消息的通道
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 配置 MQTT 消息接收适配器
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound(
            MqttPahoClientFactory mqttClientFactory,
            MessageChannel mqttInputChannel) {
        String[] topics = new String[]{
                mqttProp.getTopicOutPutDataReal(),
                mqttProp.getTopicOutPutDataMinute(),
                mqttProp.getTopicOutPutDataHour(),
                mqttProp.getTopicOutPutDataDay(),
                mqttProp.getTopicDeviceDataPlc(),
                mqttProp.getTopicApprovalResult()
        };
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(mqttProp.getClientId() + "-inbound",
                        mqttClientFactory, topics
                );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttProp.getQos());
        adapter.setOutputChannel(mqttInputChannel);

        return adapter;
    }

    // 配置消息处理器 - 直接使用 EnhancedMqttMessageHandler
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return mqttMessageHandlerSelf;
    }
}

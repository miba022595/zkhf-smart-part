package com.zkhf.epmis.process.mqtt.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MqttProp {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.connection-timeout:30}")
    private int connectionTimeout;

    @Value("${mqtt.keep-alive-interval:60}")
    private int keepAliveInterval;

    @Value("${mqtt.clean-session:true}")
    private boolean cleanSession;

    @Value("${mqtt.topics.out-put-data-real:out_put_data_real}")
    private String topicOutPutDataReal;

    @Value("${mqtt.topics.out-put-data-minute:out_put_data_minute}")
    private String topicOutPutDataMinute;

    @Value("${mqtt.topics.out-put-data-hour:out_put_data_hour}")
    private String topicOutPutDataHour;

    @Value("${mqtt.topics.out-put-data-day:out_put_data_day}")
    private String topicOutPutDataDay;

    @Value("${mqtt.topics.device-data-plc:device_data_plc}")
    private String topicDeviceDataPlc;

    @Value("${mqtt.topics.approval-result:approval_result}")
    private String topicApprovalResult;
}

package com.zkhf.epmis.process.mqtt.service;

import com.zkhf.epmis.process.mqtt.config.MqttProp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class DiskBackupService {

    @Value("${epmis.online-monitoring.fallback-dir:fallback/}")
    private String BASE_DIR;

    private MqttProp mqttProp;
    @Autowired
    public void setMqttProp(MqttProp mqttProp) {
        this.mqttProp = mqttProp;
    }

    public void saveRealData(String messageId, String payload) {
        saveToFile(BASE_DIR + mqttProp.getTopicOutPutDataReal() + "/", messageId, payload);
    }

    public void saveMinuteData(String messageId, String payload) {
        saveToFile(BASE_DIR + mqttProp.getTopicOutPutDataMinute() + "/", messageId, payload);
    }

    public void saveHourData(String messageId, String payload) {
        saveToFile(BASE_DIR + mqttProp.getTopicOutPutDataHour() + "/", messageId, payload);
    }

    public void saveDayData(String messageId, String payload) {
        saveToFile(BASE_DIR + mqttProp.getTopicOutPutDataDay() + "/", messageId, payload);
    }

    public void savePlcData(String messageId, String payload) {
        saveToFile(BASE_DIR + mqttProp.getTopicDeviceDataPlc() + "/", messageId, payload);
    }

    private void saveToFile(String dir, String messageId, String payload) {
        try {
            Files.createDirectories(Paths.get(dir));
            Path targetFile = Paths.get(dir + messageId + ".json");
            String content = messageId + "\n" + payload;
            Files.writeString(targetFile, content);
        } catch (Exception e) {
            log.error("落盘失败: messageId={}", messageId, e);
        }
    }
}
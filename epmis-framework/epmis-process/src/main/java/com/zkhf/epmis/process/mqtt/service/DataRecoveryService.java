package com.zkhf.epmis.process.mqtt.service;

import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.process.mqtt.config.MqttProp;
import com.zkhf.epmis.process.mqtt.domain.ParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
public class DataRecoveryService {

    @Value("${epmis.online-monitoring.fallback-dir:fallback/}")
    private String BASE_DIR;

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

    /**
     * 定时恢复任务
     * 启动延迟5分钟执行，之后每5分钟执行一次
     */
    @Scheduled(initialDelay = 300000, fixedDelay = 300000)
    public void recover() {
        log.info("开始执行恢复任务");
        while (true) {
            File file = findOneFile();
            if (file == null) {
                log.debug("没有待恢复的文件");
                break;
            }
            log.info("找到待恢复文件: {}", file.getName());
            try {
                // 解析文件
                ParseResult parseResult = parseFile(file);
                if (parseResult.isBadFile()) {
                    Files.delete(file.toPath());
                    log.warn("文件损坏，已删除，继续处理下一个: {}", file.getName());
                    continue;
                }
                // 调用业务接口
                boolean success = callBusinessService(file, parseResult);
                if (success) {
                    Files.delete(file.toPath());
                    log.info("恢复成功，已删除: {}", file.getName());
                } else {
                    log.warn("业务调用失败，停止本次恢复任务: {}", file.getName());
                    break;
                }
            } catch (Exception e) {
                log.error("处理文件异常: {}", file.getName(), e);
                break;
            }
        }
        log.info("恢复任务执行完成");
    }

    /**
     * 找一个待处理的文件
     * 使用 DirectoryStream 惰性加载，找到第一个即返回
     */
    private File findOneFile() {
        // 目录顺序与枚举保持一致
        String[] dirNames = {
                mqttProp.getTopicOutPutDataReal(),
                mqttProp.getTopicOutPutDataMinute(),
                mqttProp.getTopicOutPutDataHour(),
                mqttProp.getTopicOutPutDataDay(),
                mqttProp.getTopicDeviceDataPlc()
        };
        for (String dirName : dirNames) {
            Path dirPath = Paths.get(BASE_DIR + dirName);
            if (!Files.exists(dirPath)) {
                continue;
            }
            // 使用 DirectoryStream 惰性加载
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath,
                    path -> path.toString().endsWith(".json"))) {
                for (Path path : stream) {
                    // 找到第一个文件就直接返回
                    return path.toFile();
                }
            } catch (Exception e) {
                log.warn("扫描目录失败: {}", dirName, e);
            }
        }
        return null;
    }

    /**
     * 解析文件
     */
    private ParseResult parseFile(File file) {
        ParseResult result = new ParseResult();
        result.setBadFile(true);
        try {
            List<String> lines = Files.readAllLines(file.toPath());

            if (lines.size() < 2) {
                log.warn("文件格式错误(行数={}): {}", lines.size(), file.getName());
                return result;
            }

            String messageId = lines.get(0);
            String payload = lines.get(1);

            if (messageId == null || messageId.trim().isEmpty()) {
                log.warn("messageId为空: {}", file.getName());
                return result;
            }

            if (payload == null || payload.trim().isEmpty()) {
                log.warn("payload为空: {}", file.getName());
                return result;
            }
            result.setBadFile(false);
            result.setMessageId(messageId);
            result.setPayload(payload);
            result.setParentDir(file.getParentFile().getName());
            return result;
        } catch (Exception e) {
            log.warn("读取文件失败: {}", file.getName(), e);
            return result;
        }
    }

    /**
     * 调用业务服务
     */
    private boolean callBusinessService(File file, ParseResult parseResult) {
        try {
            String dirName = parseResult.getParentDir();

            if (dirName.equals(mqttProp.getTopicOutPutDataReal())) {
                mqttMessageService.handleData(parseResult.getMessageId(), parseResult.getPayload(), DataTypeEnum.real.code);
            } else if (dirName.equals(mqttProp.getTopicOutPutDataMinute())) {
                mqttMessageService.handleData(parseResult.getMessageId(), parseResult.getPayload(), DataTypeEnum.minute.code);
            } else if (dirName.equals(mqttProp.getTopicOutPutDataHour())) {
                mqttMessageService.handleData(parseResult.getMessageId(), parseResult.getPayload(), DataTypeEnum.hour.code);
            } else if (dirName.equals(mqttProp.getTopicOutPutDataDay())) {
                mqttMessageService.handleData(parseResult.getMessageId(), parseResult.getPayload(), DataTypeEnum.day.code);
            } else if (dirName.equals(mqttProp.getTopicDeviceDataPlc())) {
                mqttMessageService.dealPlcData(parseResult.getMessageId(), parseResult.getPayload());
            } else {
                return false;
            }
            return true;

        } catch (Exception e) {
            log.warn("业务调用失败: {}", file.getName(), e);
            return false;
        }
    }
}
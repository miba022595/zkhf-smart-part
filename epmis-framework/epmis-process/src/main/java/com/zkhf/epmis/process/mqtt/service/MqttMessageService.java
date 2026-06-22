package com.zkhf.epmis.process.mqtt.service;

import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.mqtt.MqttMsgMapper;
import com.zkhf.epmis.process.mqtt.domain.MqttMsgInfo;
import com.zkhf.epmis.process.mqtt.domain.MqttPlcData;
import com.zkhf.epmis.process.mqtt.domain.RealCacheData;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MqttMessageService {
    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private MqttMsgMapper mqttMsgMapper;
    @Autowired
    public void setMqttMsgMapper(MqttMsgMapper mqttMsgMapper) {
        this.mqttMsgMapper = mqttMsgMapper;
    }

    private MessageAlarmService messageAlarmService;
    @Autowired
    public void setMqttMessageAlarmService(MessageAlarmService messageAlarmService) {
        this.messageAlarmService = messageAlarmService;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    private DiskBackupService diskBackupService;
    @Autowired
    public void setDiskBackupService(DiskBackupService diskBackupService) {
        this.diskBackupService = diskBackupService;
    }

    @PostConstruct // 启动后获取排口信息
    public void init() {
        try {
            // 获取企业和排口信息
            List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
            if (null != outPutList) {
                // 更新内存
                outPutList.forEach( e -> RedisCacheUtils.outPutInfoMap.put(e.getMnNum(), e));
            }
        } catch (Exception e) {
            log.error("更新企业和排口缓存信息失败", e);
        }
    }

    @Async
    public void handleRealData(String messageId, String payload) {
        try {
            log.debug("处理实时数据 messageId {}", messageId);
            handleData(messageId, payload, DataTypeEnum.real.code);
            log.debug("实时数据处理完成 messageId {}", messageId);
        } catch (Exception e) {
            log.error("实时数据处理失败，落盘保存: messageId {}", messageId, e);
            diskBackupService.saveRealData(messageId, payload);
        }
    }

    @Async
    public void handleMinuteData(String messageId, String payload) {
        try {
            log.debug("处理分钟数据 messageId {}", messageId);
            handleData(messageId, payload, DataTypeEnum.minute.code);
            log.debug("分钟数据处理完成 messageId {}", messageId);
        } catch (Exception e) {
            log.error("分钟数据处理失败，落盘保存: messageId {}", messageId, e);
            diskBackupService.saveMinuteData(messageId, payload);
        }
    }

    @Async
    public void handleHourData(String messageId, String payload) {
        try {
            log.debug("处理小时数据 messageId {}", messageId);
            handleData(messageId, payload, DataTypeEnum.hour.code);
            log.debug("小时数据处理完成 messageId {}", messageId);
        } catch (Exception e) {
            log.error("小时数据处理失败，落盘保存: messageId {}", messageId, e);
            diskBackupService.saveHourData(messageId, payload);
        }
    }

    @Async
    public void handleDayData(String messageId, String payload) {
        try {
            log.debug("处理日数据 messageId {}", messageId);
            handleData(messageId, payload, DataTypeEnum.day.code);
            log.debug("日数据处理完成 messageId {}", messageId);
        } catch (Exception e) {
            log.error("日数据处理失败，落盘保存: messageId {}", messageId, e);
            diskBackupService.saveDayData(messageId, payload);
        }
    }

    public void handleData(String messageId, String payload, Integer dataType) {
        // 处理数据
        MqttMsgInfo info = JSONObject.parseObject(payload, MqttMsgInfo.class);
        // 获取排口信息
        OutPutInfo outInfo = RedisCacheUtils.outPutInfoMap.get(info.getMnNum());
        if (null == outInfo) {
            log.error("未知的排口 {}", info.getMnNum());
            return;
        }
        Map<String, Object> monitorData = info.getMonitorData();
        if (null == monitorData || monitorData.isEmpty()) {
            log.error("数据为空 {}", info.getMnNum());
            return;
        }
        // 使用String.format确保3位数字，不足补零
        String formattedDataType = String.format("%03d", dataType);
        info.setOutId(info.getMonitorTime().format(dtf) + formattedDataType);
        info.setDataAlarm(0);
        info.setDataTypeInt(dataType);
        // 数据表名称组成：t_data_out_年份，5316150条可以了)_排口id
        String tableSuffix = DateUtils.getTableYear(info.getMonitorTime()) + "_" + outInfo.getOutPutId().toLowerCase();
        // 创建表-不存在时
        mqttMsgMapper.createTable(tableSuffix);
        // 查看是否存在历史数据
        String oldPollutantInfo = mqttMsgMapper.selectOutPollData(info.getOutId(), tableSuffix);
        // 检查历史数据，添加不存在的到新数据
        putAll(oldPollutantInfo, monitorData);
        // 设置新的数据
        info.setDataInfo(JSONObject.toJSONString(monitorData));
        if (StringUtils.isEmpty(oldPollutantInfo)) {
            // 无历史数据，插入新的
            mqttMsgMapper.insertOutPollData(info, tableSuffix);
        } else {
            // 存在历史数据，更新
            mqttMsgMapper.updateOutPollData(info, tableSuffix);
        }
        // 判断报警处理
        messageAlarmService.handleAlarm(messageId, info, outInfo);
        // 实时数据把消息缓存下来，用于实时一览功能
        if (DataTypeEnum.real.code.equals(dataType)) {
            RealCacheData cache = new RealCacheData();
            cache.setTime(info.getMonitorTime());
            cache.setData(monitorData);
            redisCacheUtils.setRealData(outInfo.getOutPutId(), cache);
        }
    }

    private void putAll(String oldPollutantInfo, Map<String, Object> monitorData) {
        if (StringUtils.isEmpty(oldPollutantInfo)) {
            return;
        }
        try {
            Map<String, Object> oldMap = JSONObject.parseObject(oldPollutantInfo);
            oldMap.forEach( (k, v) -> {
                if (!monitorData.containsKey(k)) {
                    monitorData.put(k, v);
                }
            });
        } catch (Exception e) {
            log.error("历史数据合并失败", e);
        }
    }

    @Async
    public void handlePlcData(String messageId, String payload) {
        try {
            log.debug("处理plc数据 messageId {}, payload {}", messageId, payload);
            dealPlcData(messageId, payload);
            log.debug("plc数据处理完成 messageId {}", messageId);
        } catch (Exception e) {
            log.error("plc数据处理失败，落盘保存: messageId {}", messageId, e);
            diskBackupService.savePlcData(messageId, payload);
        }
    }

    public void dealPlcData(String messageId, String payload) {
        // 处理数据
        MqttPlcData info = JSONObject.parseObject(payload, MqttPlcData.class);
        if (null == info.getUnitId() || info.getUnitId() < 0 || null == info.getFnCode() || info.getFnCode() < 0) {
            log.error("处理plc数据 messageId {}, 数据异常 {}", messageId, info);
            return;
        }
        // 直接入库，查询时进行字段对应
        if (null != info.getTime()) {
            info.setReportTime(DateUtils.strToLocalDateTime(info.getTime(), DateUtils.dtfC));
        } else {
            info.setReportTime(LocalDateTime.now());
        }
        info.setId(UlidCreator.getMonotonicUlid().toString());
        info.setType(info.getUnitId() + "_" + info.getFnCode());
        // 实时数据更新
        mqttMsgMapper.deletePlcRealData(info.getType());
        mqttMsgMapper.insertPlcRealData(info);
        // 历史数据入库
        // 数据表名称组成：t_plc_raw_data_年月，按月分表
        String tableSuffix = info.getReportTime().format(DateUtils.yym);
        // 创建表-不存在时
        mqttMsgMapper.createPlcDataTable(tableSuffix);
        // 数据入库
        mqttMsgMapper.insertPlcData(info, tableSuffix);
    }
}

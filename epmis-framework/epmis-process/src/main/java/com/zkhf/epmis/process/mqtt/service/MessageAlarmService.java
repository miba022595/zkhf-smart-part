package com.zkhf.epmis.process.mqtt.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.constant.CacheConstants;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.redis.RedisCache;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.BaseDurAlarm;
import com.zkhf.epmis.process.base.domain.OutPutAlarmConf;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.OutPutPollInfo;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.base.BaseAlarmMapper;
import com.zkhf.epmis.process.mqtt.domain.MqttMsgInfo;
import com.zkhf.epmis.process.send.robotPhone.AliYunVoicePush;
import com.zkhf.epmis.process.send.weixin.WeComSend;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageAlarmService {

    private final String alarmCycleKey = CacheConstants.DATA_CACHE_KEY + "ent:data:alarmCycle:";

    /** 恒值时，缓存上一个值的key */
    private final String lastValue = "lastValue";
    /** 有持续次数报警监测时，缓存开始的时间的key */
    private final String firstTime = "firstTime";

    private BaseAlarmMapper baseAlarmMapper;
    @Autowired
    public void setBaseAlarmMapper(BaseAlarmMapper baseAlarmMapper) {
        this.baseAlarmMapper = baseAlarmMapper;
    }

    private WeComSend weComSend;

    @Autowired
    public void setWeComSend(WeComSend weComSend) {
        this.weComSend = weComSend;
    }

    private RedisCache redisCache;
    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    private SseMessagingService sseMessagingService;
    @Autowired
    public void setSseMessagingService(SseMessagingService sseMessagingService) {
        this.sseMessagingService = sseMessagingService;
    }

    private AliYunVoicePush aliYunVoicePush;
    @Autowired
    public void setAliYunVoicePush(AliYunVoicePush aliYunVoicePush) {
        this.aliYunVoicePush = aliYunVoicePush;
    }

    @Async
    public void handleAlarm(String messageId, MqttMsgInfo info, OutPutInfo outInfo) {
        log.debug("报警处理 messageId {}, 企业 {}，排口 {} 无污染物勾选", messageId, outInfo.getEntName(), outInfo.getOutPutName());
        // 通过mn号查询排口污染物的基本信息包含报警类型字段
        Map<String, OutPutPollInfo> pollInfoMap = RedisCacheUtils.outPutPollInfoMap.get(info.getMnNum());
        if (null == pollInfoMap || pollInfoMap.isEmpty()) {
            log.error("企业 {}，排口 {} 无污染物勾选", outInfo.getEntName(), outInfo.getOutPutName());
            return;
        }
        Map<Integer, OutPutAlarmConf> confMap = RedisCacheUtils.outPutAlarmConfMap.get(outInfo.getOutPutId());
        if (null == confMap || confMap.isEmpty()) {
            log.error("企业 {}，排口 {} 未进行报警参数配置", outInfo.getEntName(), outInfo.getOutPutName());
            return;
        }
        // 依据排口类型进行不同报警的处理
        if (OutPutTypeEnum.OUT_PUT_FS.code.equals(outInfo.getOutPutType())) {
            handleAlarmFeiS(info, outInfo, pollInfoMap, confMap);
        } else if (OutPutTypeEnum.OUT_PUT_FQ.code.equals(outInfo.getOutPutType())) {
            handleAlarmFeiQ(info, outInfo, pollInfoMap, confMap);
        } else { // 其他类型的排口报警方式先弄成一样的，有区别的再加else if
            handleAlarmOther(info, outInfo, pollInfoMap, confMap);
        }
        log.debug("报警处理完成 messageId {}", messageId);
    }

    /**
     * 废水报警处理
     * 小时超限报警、零值报警、负值报警
     * 分钟负值预警
     */
    public void handleAlarmFeiS(MqttMsgInfo info, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap, Map<Integer, OutPutAlarmConf> confMap) {
        // 提取监测数据
        Map<String, Object> monitorData = info.getMonitorData();
        // 循环监测数据
        List<BaseDurAlarm> alarmList = new ArrayList<>();
        List<BaseDurAlarm> warnList = new ArrayList<>();
        OutPutAlarmConf alarmConf;
        String cycleCacheKey, firstTimeKey;
        Map<String, JSONObject> pollCycleMap = new HashMap<>();
        if (DataTypeEnum.hour.code.equals(info.getDataTypeInt())) {
            cycleCacheKey = alarmCycleKey + outInfo.getOutPutId() + ":" + DataTypeEnum.hour.code;
            JSONObject alarmCycle = getCacheData(cycleCacheKey);
            // 配置勾选的污染物列表
            List<String> allPollCode = new ArrayList<>(pollInfoMap.keySet());
            String alarmMsg = null;
            for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
                String k = entry.getKey();
                // 判断排口勾选的污染物
                if (!pollInfoMap.containsKey(k)) {
                    continue;
                }
                JSONObject pollCycle = alarmCycle.getJSONObject(k);
                if (null == pollCycle) {
                    pollCycle = new JSONObject();
                    alarmCycle.put(k, pollCycle);
                }
                // 移除存在的污染物
                allPollCode.remove(k);
                // 上报折算值采用折算值，无折算值用实测值
                OutPutPollInfo pollInfo = pollInfoMap.get(k);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)entry.getValue();
                BigDecimal value = getSampleValue(data);
                if (null == value) {
                    continue;
                }
                Integer alarmType = null;
                // 计算上限报警，判断报警配置是否开启、数据类型是否勾选、排口状态是否支持
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_LARGE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus())
                        && null != pollInfo.getOverMaxValue() && value.compareTo(pollInfo.getOverMaxValue()) > 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_LARGE.code;
                    alarmMsg = value + "(" + pollInfo.getOverMaxValue() + ")";
                }
                // 计算下限报警
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_SMALL.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && null != pollInfo.getOverMinValue() && value.compareTo(pollInfo.getOverMinValue()) < 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_SMALL.code;
                    alarmMsg = value + "(" + pollInfo.getOverMinValue() + ")";
                }
                // 数据异常判断
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_ZERO.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.ALARM_ZERO_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0 && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启零值异常，判断监测值是否为0;
                    if (value.compareTo(BigDecimal.ZERO) == 0) {
                        // 保存触发报警的时间
                        if (!pollCycle.containsKey(firstTimeKey)) {
                            pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                        }
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.ALARM_ZERO_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.ALARM_ZERO.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.ALARM_ZERO_STR, olcCount);
                    } else {
                        pollCycle.remove(AlarmDetailTypeEnum.ALARM_ZERO_STR);
                        pollCycle.remove(firstTimeKey);
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.ALARM_ZERO_STR);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_CONSTANT.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.ALARM_CONSTANT_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0 && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启恒值异常，判断监测值是否恒定;
                    BigDecimal last = pollCycle.getBigDecimal(lastValue);
                    if (null != last && value.compareTo(last) == 0) {
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.ALARM_CONSTANT_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.ALARM_CONSTANT.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.ALARM_CONSTANT_STR, olcCount);
                    } else {
                        pollCycle.put(AlarmDetailTypeEnum.ALARM_CONSTANT_STR, 1);
                        pollCycle.put(lastValue, value);
                        pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.ALARM_CONSTANT_STR);
                    pollCycle.remove(lastValue);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_NEGATIVE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsNegativeAlarm())) {
                    // 开启负值异常，判断监测值是否为负数0;
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        alarmType = AlarmDetailTypeEnum.ALARM_NEGATIVE.code;
                        alarmMsg = value.toString();
                    }
                }
                // 有报警发生
                addAlarmInfo(alarmList, outInfo.getOutPutId(), k, info.getDataTypeInt(), alarmType, info.getMonitorTime(), alarmMsg);
            }
            // 列表存在数据则表示有污染物未上报
            if (!allPollCode.isEmpty()) {
                // 有报警发生
                addAlarmInfo(alarmList, outInfo.getOutPutId(), "", info.getDataTypeInt(), AlarmDetailTypeEnum.ALARM_HOUR_IMPERFECT.code, info.getMonitorTime(), String.join(",", allPollCode));
            }
            redisCache.setCacheObject(cycleCacheKey, alarmCycle);
        } else if (DataTypeEnum.minute.code.equals(info.getDataTypeInt())) {
            cycleCacheKey = alarmCycleKey + outInfo.getOutPutId() + ":" + DataTypeEnum.minute.code;
            JSONObject alarmCycle = getCacheData(cycleCacheKey);
            String alarmMsg = null;
            for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
                String k = entry.getKey();
                // 判断排口勾选的污染物
                if (!pollInfoMap.containsKey(k)) {
                    continue;
                }
                JSONObject pollCycle = alarmCycle.getJSONObject(k);
                if (null == pollCycle) {
                    pollCycle = new JSONObject();
                    alarmCycle.put(k, pollCycle);
                }
                // 上报折算值采用折算值，无折算值用实测值
                OutPutPollInfo pollInfo = pollInfoMap.get(k);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)entry.getValue();
                BigDecimal value = getSampleValue(data);
                if (null == value) {
                    continue;
                }
                Integer alarmType = null;
                // 数据异常判断
                alarmConf = confMap.get(AlarmDetailTypeEnum.WARN_ZERO.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.WARN_ZERO_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启零值异常，判断监测值是否为0;
                    if (value.compareTo(BigDecimal.ZERO) == 0) {
                        // 保存触发报警的时间
                        if (!pollCycle.containsKey(firstTimeKey)) {
                            pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                        }
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.WARN_ZERO_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.WARN_ZERO.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.WARN_ZERO_STR, olcCount);
                    } else {
                        pollCycle.remove(AlarmDetailTypeEnum.WARN_ZERO_STR);
                        pollCycle.remove(firstTimeKey);
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.WARN_ZERO_STR);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.WARN_CONSTANT.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.WARN_CONSTANT_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0 && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启恒值异常，判断监测值是否恒定;
                    BigDecimal last = pollCycle.getBigDecimal(lastValue);
                    if (null != last && value.compareTo(last) == 0) {
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.WARN_CONSTANT_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.WARN_CONSTANT.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.WARN_CONSTANT_STR, olcCount);
                    } else {
                        pollCycle.put(AlarmDetailTypeEnum.WARN_CONSTANT_STR, 1);
                        pollCycle.put(lastValue, value);
                        pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.WARN_CONSTANT_STR);
                    pollCycle.remove(lastValue);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.WARN_NEGATIVE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsNegativeAlarm())) {
                    // 开启负值异常，判断监测值是否为负数;
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        alarmType = AlarmDetailTypeEnum.WARN_NEGATIVE.code;
                        alarmMsg = value.toString();
                    }
                }
                // 有预警发生
                if (null != alarmType) {
                    BaseDurAlarm warn = new BaseDurAlarm();
                    warn.setAlarmId(UlidCreator.getMonotonicUlid().toString());
                    warn.setOutPutId(outInfo.getOutPutId());
                    warn.setPollutantCode(entry.getKey());
                    warn.setDataType(DataTypeEnum.minute.code);
                    warn.setAlarmType(alarmType);
                    warn.setAlarmTime(info.getMonitorTime());
                    warn.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
                    warn.setAlarmMsg(alarmMsg);
                    warnList.add(warn);
                }
            }
            redisCache.setCacheObject(cycleCacheKey, alarmCycle);
        }
        // 报警数据设置
        updateAlarm(info, alarmList, outInfo, pollInfoMap, pollCycleMap);
        // 删除旧的预警信息
        baseAlarmMapper.deleteOldWarn(outInfo.getOutPutId(), DataTypeEnum.minute.code,
                Arrays.asList(AlarmDetailTypeEnum.WARN_ZERO.code, AlarmDetailTypeEnum.WARN_CONSTANT.code, AlarmDetailTypeEnum.WARN_NEGATIVE.code));
        if (!warnList.isEmpty()) {
            // 插入预警信息
            baseAlarmMapper.batchInsertWarn(warnList);
            sendWXWarnMessage(warnList, outInfo, pollInfoMap);
        }
    }

    /**
     * 废气报警处理
     * 小时超限报警、零值报警、负值报警
     * 分钟超限报警、负值预警
     */
    private void handleAlarmFeiQ(MqttMsgInfo info, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap, Map<Integer, OutPutAlarmConf> confMap) {
        // 提取监测数据
        Map<String, Object> monitorData = info.getMonitorData();
        // 循环监测数据
        List<BaseDurAlarm> alarmList = new ArrayList<>();
        List<BaseDurAlarm> warnList = new ArrayList<>();
        OutPutAlarmConf alarmConf;
        String cycleCacheKey, firstTimeKey;
        Map<String, JSONObject> pollCycleMap = new HashMap<>();
        if (DataTypeEnum.hour.code.equals(info.getDataTypeInt())) {
            cycleCacheKey = alarmCycleKey + outInfo.getOutPutId() + ":" + DataTypeEnum.hour.code;
            JSONObject alarmCycle = getCacheData(cycleCacheKey);
            // 配置勾选的污染物列表
            List<String> allPollCode = new ArrayList<>(pollInfoMap.keySet());
            String alarmMsg = null;
            for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
                String k = entry.getKey();
                // 判断排口勾选的污染物
                if (!pollInfoMap.containsKey(k)) {
                    continue;
                }
                JSONObject pollCycle = alarmCycle.getJSONObject(k);
                if (null == pollCycle) {
                    pollCycle = new JSONObject();
                    alarmCycle.put(k, pollCycle);
                }
                // 移除存在的污染物
                allPollCode.remove(k);
                // 上报折算值采用折算值，无折算值用实测值
                OutPutPollInfo pollInfo = pollInfoMap.get(k);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)entry.getValue();
                BigDecimal value = getSampleValue(data);
                if (null == value) {
                    continue;
                }
                Integer alarmType = null;
                // 计算上限报警，判断报警配置是否开启、数据类型是否勾选、排口状态是否支持
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_LARGE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus())
                        && null != pollInfo.getOverMaxValue() && value.compareTo(pollInfo.getOverMaxValue()) > 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_LARGE.code;
                    alarmMsg = value + "(" + pollInfo.getOverMaxValue() + ")";
                }
                // 计算下限报警
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_SMALL.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && null != pollInfo.getOverMinValue() && value.compareTo(pollInfo.getOverMinValue()) < 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_SMALL.code;
                    alarmMsg = value + "(" + pollInfo.getOverMinValue() + ")";
                }
                // 数据异常判断
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_ZERO.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.ALARM_ZERO_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0 && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启零值异常，判断监测值是否为0;
                    if (value.compareTo(BigDecimal.ZERO) == 0) {
                        // 保存触发报警的时间
                        if (!pollCycle.containsKey(firstTimeKey)) {
                            pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                        }
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.ALARM_ZERO_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.ALARM_ZERO.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.ALARM_ZERO_STR, olcCount);
                    } else {
                        pollCycle.remove(AlarmDetailTypeEnum.ALARM_ZERO_STR);
                        pollCycle.remove(firstTimeKey);
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.ALARM_ZERO_STR);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_CONSTANT.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.ALARM_CONSTANT_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0 && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启恒值异常，判断监测值是否恒定;
                    BigDecimal last = pollCycle.getBigDecimal(lastValue);
                    if (null != last && value.compareTo(last) == 0) {
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.ALARM_CONSTANT_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.ALARM_CONSTANT.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.ALARM_CONSTANT_STR, olcCount);
                    } else {
                        pollCycle.put(AlarmDetailTypeEnum.ALARM_CONSTANT_STR, 1);
                        pollCycle.put(lastValue, value);
                        pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.ALARM_CONSTANT_STR);
                    pollCycle.remove(lastValue);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_NEGATIVE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsNegativeAlarm())) {
                    // 开启负值异常，判断监测值是否为负数0;
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        alarmType = AlarmDetailTypeEnum.ALARM_NEGATIVE.code;
                        alarmMsg = value.toString();
                    }
                }
                // 有报警发生
                addAlarmInfo(alarmList, outInfo.getOutPutId(), k, info.getDataTypeInt(), alarmType, info.getMonitorTime(), alarmMsg);
            }
            // 列表存在数据则表示有污染物未上报
            if (!allPollCode.isEmpty()) {
                // 有报警发生
                addAlarmInfo(alarmList, outInfo.getOutPutId(), "", info.getDataTypeInt(), AlarmDetailTypeEnum.ALARM_HOUR_IMPERFECT.code, info.getMonitorTime(), String.join(",", allPollCode));
            }
            redisCache.setCacheObject(cycleCacheKey, alarmCycle);
        } else if (DataTypeEnum.minute.code.equals(info.getDataTypeInt())) {
            cycleCacheKey = alarmCycleKey + outInfo.getOutPutId() + ":" + DataTypeEnum.minute.code;
            JSONObject alarmCycle = getCacheData(cycleCacheKey);
            String alarmMsg = null;
            for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
                String k = entry.getKey();
                // 判断排口勾选的污染物
                if (!pollInfoMap.containsKey(k)) {
                    continue;
                }
                JSONObject pollCycle = alarmCycle.getJSONObject(k);
                if (null == pollCycle) {
                    pollCycle = new JSONObject();
                    alarmCycle.put(k, pollCycle);
                }
                // 上报折算值采用折算值，无折算值用实测值
                OutPutPollInfo pollInfo = pollInfoMap.get(k);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)entry.getValue();
                BigDecimal value = getSampleValue(data);
                if (null == value) {
                    continue;
                }
                Integer alarmType = null;
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_LARGE.code);
                // 计算上限报警
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus())
                        && null != pollInfo.getOverMaxValue() && value.compareTo(pollInfo.getOverMaxValue()) > 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_LARGE.code;
                    alarmMsg = value + "(" + pollInfo.getOverMaxValue() + ")";
                    // 有报警发生
                    addAlarmInfo(alarmList, outInfo.getOutPutId(), k, info.getDataTypeInt(), alarmType, info.getMonitorTime(), alarmMsg);
                    continue;
                }
                // 数据异常判断
                alarmConf = confMap.get(AlarmDetailTypeEnum.WARN_ZERO.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.WARN_ZERO_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启零值异常，判断监测值是否为0;
                    if (value.compareTo(BigDecimal.ZERO) == 0) {
                        // 保存触发报警的时间
                        if (!pollCycle.containsKey(firstTimeKey)) {
                            pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                        }
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.WARN_ZERO_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.WARN_ZERO.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.WARN_ZERO_STR, olcCount);
                    } else {
                        pollCycle.remove(AlarmDetailTypeEnum.WARN_ZERO_STR);
                        pollCycle.remove(firstTimeKey);
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.WARN_ZERO_STR);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.WARN_CONSTANT.code);
                firstTimeKey = firstTime + AlarmDetailTypeEnum.WARN_CONSTANT_STR;
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null != alarmConf.getDataCycle() && alarmConf.getDataCycle() > 0 && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsZeroAlarm())) {
                    // 开启恒值异常，判断监测值是否恒定;
                    BigDecimal last = pollCycle.getBigDecimal(lastValue);
                    if (null != last && value.compareTo(last) == 0) {
                        // 条数判断
                        int olcCount = pollCycle.getIntValue(AlarmDetailTypeEnum.WARN_CONSTANT_STR) + 1;
                        if (olcCount >= alarmConf.getDataCycle()) {
                            alarmType = AlarmDetailTypeEnum.WARN_CONSTANT.code;
                            alarmMsg = value.toString();
                            // 发生报警时缓存参数信息
                            pollCycleMap.put(outInfo.getOutPutId() + "_" + info.getDataTypeInt() + "_" + k + "_" + alarmType, pollCycle);
                        }
                        pollCycle.put(AlarmDetailTypeEnum.WARN_CONSTANT_STR, olcCount);
                    } else {
                        pollCycle.put(AlarmDetailTypeEnum.WARN_CONSTANT_STR, 1);
                        pollCycle.put(lastValue, value);
                        pollCycle.put(firstTimeKey, info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s));
                    }
                } else {
                    pollCycle.remove(AlarmDetailTypeEnum.WARN_CONSTANT_STR);
                    pollCycle.remove(lastValue);
                    pollCycle.remove(firstTimeKey);
                }
                alarmConf = confMap.get(AlarmDetailTypeEnum.WARN_NEGATIVE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && Constants.POLL_CONF_ON.equals(pollInfo.getIsNegativeAlarm())) {
                    // 开启负值异常，判断监测值是否为负数;
                    if (value.compareTo(BigDecimal.ZERO) < 0) {
                        alarmType = AlarmDetailTypeEnum.WARN_NEGATIVE.code;
                        alarmMsg = value.toString();
                    }
                }
                // 有预警发生
                if (null != alarmType) {
                    BaseDurAlarm warn = new BaseDurAlarm();
                    warn.setAlarmId(UlidCreator.getMonotonicUlid().toString());
                    warn.setOutPutId(outInfo.getOutPutId());
                    warn.setPollutantCode(entry.getKey());
                    warn.setDataType(DataTypeEnum.minute.code);
                    warn.setAlarmType(alarmType);
                    warn.setAlarmTime(info.getMonitorTime());
                    warn.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
                    warn.setAlarmMsg(alarmMsg);
                    warnList.add(warn);
                }
            }
            redisCache.setCacheObject(cycleCacheKey, alarmCycle);
        }
        // 报警数据设置
        updateAlarm(info, alarmList, outInfo, pollInfoMap, pollCycleMap);
        // 删除旧的预警信息
        baseAlarmMapper.deleteOldWarn(outInfo.getOutPutId(), DataTypeEnum.minute.code,
                Arrays.asList(AlarmDetailTypeEnum.WARN_ZERO.code, AlarmDetailTypeEnum.WARN_CONSTANT.code, AlarmDetailTypeEnum.WARN_NEGATIVE.code));
        if (!warnList.isEmpty()) {
            // 插入预警信息
            baseAlarmMapper.batchInsertWarn(warnList);
            sendWXWarnMessage(warnList, outInfo, pollInfoMap);
        }
    }

    /**
     * 扬尘报警处理
     * 分钟超标
     * 小时超标
     */
    private void handleAlarmOther(MqttMsgInfo info, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap, Map<Integer, OutPutAlarmConf> confMap) {
        // 提取监测数据
        Map<String, Object> monitorData = info.getMonitorData();
        // 循环监测数据
        List<BaseDurAlarm> alarmList = new ArrayList<>();
        OutPutAlarmConf alarmConf;
        String cycleCacheKey;
        if (DataTypeEnum.hour.code.equals(info.getDataTypeInt())) {
            cycleCacheKey = alarmCycleKey + outInfo.getOutPutId() + ":" + DataTypeEnum.hour.code;
            JSONObject alarmCycle = getCacheData(cycleCacheKey);
            String alarmMsg = null;
            for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
                String k = entry.getKey();
                // 判断排口勾选的污染物
                if (!pollInfoMap.containsKey(k)) {
                    continue;
                }
                JSONObject pollCycle = alarmCycle.getJSONObject(k);
                if (null == pollCycle) {
                    pollCycle = new JSONObject();
                    alarmCycle.put(k, pollCycle);
                }
                // 上报折算值采用折算值，无折算值用实测值
                OutPutPollInfo pollInfo = pollInfoMap.get(k);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)entry.getValue();
                BigDecimal value = getSampleValue(data);
                if (null == value) {
                    continue;
                }
                Integer alarmType = null;
                // 计算上限报警，判断报警配置是否开启、数据类型是否勾选、排口状态是否支持
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_LARGE.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus())
                        && null != pollInfo.getOverMaxValue() && value.compareTo(pollInfo.getOverMaxValue()) > 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_LARGE.code;
                    alarmMsg = value + "(" + pollInfo.getOverMaxValue() + ")";
                }
                // 计算下限报警
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_SMALL.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus()) && null == alarmType
                        && null != pollInfo.getOverMinValue() && value.compareTo(pollInfo.getOverMinValue()) < 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_SMALL.code;
                    alarmMsg = value + "(" + pollInfo.getOverMinValue() + ")";
                }
                // 有报警发生
                addAlarmInfo(alarmList, outInfo.getOutPutId(), k, info.getDataTypeInt(), alarmType, info.getMonitorTime(), alarmMsg);
            }
            redisCache.setCacheObject(cycleCacheKey, alarmCycle);
        } else if (DataTypeEnum.minute.code.equals(info.getDataTypeInt())) {
            String alarmMsg = null;
            for (Map.Entry<String, Object> entry : monitorData.entrySet()) {
                String k = entry.getKey();
                // 判断排口勾选的污染物
                if (!pollInfoMap.containsKey(k)) {
                    continue;
                }
                // 上报折算值采用折算值，无折算值用实测值
                OutPutPollInfo pollInfo = pollInfoMap.get(k);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>)entry.getValue();
                BigDecimal value = getSampleValue(data);
                if (null == value) {
                    continue;
                }
                Integer alarmType = null;
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_LARGE.code);
                // 计算上限报警
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus())
                        && null != pollInfo.getOverMaxValue() && value.compareTo(pollInfo.getOverMaxValue()) > 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_LARGE.code;
                    alarmMsg = value + "(" + pollInfo.getOverMaxValue() + ")";
                }
                // 计算下限报警
                alarmConf = confMap.get(AlarmDetailTypeEnum.ALARM_SMALL.code);
                if (checkAlarmConf(alarmConf, info.getDataTypeInt(), outInfo.getOutPutStatus())
                        && null == alarmType && null != pollInfo.getOverMinValue() && value.compareTo(pollInfo.getOverMinValue()) < 0) {
                    alarmType = AlarmDetailTypeEnum.ALARM_SMALL.code;
                    alarmMsg = value + "(" + pollInfo.getOverMinValue() + ")";
                }
                // 有报警发生
                addAlarmInfo(alarmList, outInfo.getOutPutId(), k, info.getDataTypeInt(), alarmType, info.getMonitorTime(), alarmMsg);
            }
        }
        // 报警数据设置
        updateAlarm(info, alarmList, outInfo, pollInfoMap, null);
    }

    /**
     * 判断报警配置是否开启、数据类型是否勾选、排口状态是否支持
     */
    private boolean checkAlarmConf(OutPutAlarmConf alarmConf, Integer dataType, Integer outPutStatus) {
        return null != alarmConf
                && Constants.ALARM_CONF_ON.equals(alarmConf.getIsEnabled())
                && alarmConf.getDataType().contains(DataTypeEnum.getCodeStr(dataType))
                && alarmConf.getOutPutStatus().contains(OutPutStatusEnum.getCodeStr(outPutStatus));
    }

    private String getPollName(Map<String, OutPutPollInfo> pollInfoMap, List<BaseDurAlarm> list) {
        if (null == list || list.isEmpty() || null == pollInfoMap) {
            return "";
        }
        StringBuilder bu = new StringBuilder();
        for (BaseDurAlarm info : list) {
            if (pollInfoMap.containsKey(info.getPollutantCode())) {
                if (bu.length() > 0) {
                    bu.append("，");
                }
                bu.append(pollInfoMap.get(info.getPollutantCode()).getPollutantNameCn()).append(": ").append(info.getAlarmMsg());
            }
        }
        return bu.toString();
    }

    private void updateAlarm(MqttMsgInfo info, List<BaseDurAlarm> alarmList, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap,
                             Map<String, JSONObject> pollCycleMap) {
        log.debug("报警数据入库 {}", alarmList);
        // 获取排口未解除的报警
        List<Map<String, String>> activeList = baseAlarmMapper.selectAlarmList(outInfo.getOutPutId(), info.getDataTypeInt(), null, Constants.ALARM_STATUS_ACTIVE);
        Map<String, String> activeMap = new HashMap<>();
        activeList.forEach( e -> activeMap.put(MapUtil.getStr(e,"pollutant_code") + "_" + MapUtil.getStr(e,"alarm_type"), MapUtil.getStr(e,"alarm_id")));
        List<BaseDurAlarm> insertList = new ArrayList<>();
        String cacheKey = CacheConstants.DATA_CACHE_KEY + "ent:data:alarmTime:" + outInfo.getOutPutId() + ":" + info.getDataTypeInt();
        JSONObject alarmTime = getCacheData(cacheKey);
        for (BaseDurAlarm e : alarmList) {
            String key = e.getPollutantCode() + "_" + e.getAlarmType();
            alarmTime.put(key, e.getAlarmTime().format(DateUtils.yy_m_d_h_m_s));
            if (activeMap.containsKey(key)) { // 持续的报警
                activeMap.remove(key);
            } else {
                insertList.add(e);
                if (null == pollCycleMap) {
                    continue;
                }
                // 恒值报警、零值报警，发生报警的时候应该替换报警开始时间
                JSONObject pollCycle = pollCycleMap.get(e.getOutPutId() + "_" + e.getDataType() + "_" + e.getPollutantCode() + "_" + e.getAlarmType());
                if (null == pollCycle) {
                    continue;
                }
                // 发生报警时替换报警的时间
                if (AlarmDetailTypeEnum.ALARM_CONSTANT.code.equals(e.getAlarmType())) {
                    e.setAlarmTime(getPollCycleTime(pollCycle, firstTime + AlarmDetailTypeEnum.ALARM_CONSTANT_STR, info.getMonitorTime()));
                } else if (AlarmDetailTypeEnum.ALARM_ZERO.code.equals(e.getAlarmType())) {
                    e.setAlarmTime(getPollCycleTime(pollCycle, firstTime + AlarmDetailTypeEnum.ALARM_ZERO_STR, info.getMonitorTime()));
                }
            }
        }
        // 报警解除
        if (!activeMap.isEmpty()) {
            Map<String, String> updateMap = new HashMap<>();
            String nowStr = info.getMonitorTime().format(DateUtils.yy_m_d_h_m_s);
            activeMap.forEach( (k, v) -> {
                String dateStr = alarmTime.getString(k);
                if (StringUtils.isEmpty(dateStr)) {
                    updateMap.put(v, nowStr);
                } else {
                    updateMap.put(v, dateStr);
                }
            });
            baseAlarmMapper.batchUpdateDurAlarm(updateMap, Constants.ALARM_STATUS_RESOLVED, Constants.DEAL_STATUS_COMPLETED);
        }
        // 插入报警信息
        if (!insertList.isEmpty()) {
            baseAlarmMapper.batchInsertDurAlarm(insertList);
        }
        // 发送报警消息
        sendWXMessage(insertList, outInfo, pollInfoMap);
        // 机器人发送电话通知消息
        sendPhoneMessage(insertList, info, outInfo, pollInfoMap);
        redisCache.setCacheObject(cacheKey, alarmTime);
    }

    private JSONObject getCacheData(String cacheKey) {
        JSONObject cache = redisCache.getCacheObject(cacheKey);
        if (null == cache) {
            cache = new JSONObject();
        }
        return cache;
    }

    private void sendWXMessage(List<BaseDurAlarm> alarmList, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap) {
        if (alarmList.isEmpty()) {
            return;
        }
        /* 发送报警消息 */
        Map<Integer, Map<Integer, List<BaseDurAlarm>>> alarmMap = new HashMap<>();
        alarmList.forEach( e -> {
            Map<Integer, List<BaseDurAlarm>> sub;
            if (alarmMap.containsKey(e.getAlarmType())) {
                sub = alarmMap.get(e.getAlarmType());
            } else {
                sub = new HashMap<>();
                alarmMap.put(e.getAlarmType(), sub);
            }
            List<BaseDurAlarm> subList;
            if (sub.containsKey(e.getDataType())) {
                subList = sub.get(e.getDataType());
            } else {
                subList = new ArrayList<>();
                sub.put(e.getDataType(), subList);
            }
            subList.add(e);
        });
        String token = weComSend.getAccessToken();
        alarmMap.forEach( (alarmType, sub) -> sub.forEach( (dataType, list) -> {
            LocalDateTime time = list.get(0).getAlarmTime();
            String messType = null, pollName = null;
            if (AlarmDetailTypeEnum.ALARM_ZERO.code.equals(alarmType)) {
                messType = "【数据异常报警-零值】 ";
                pollName = getPollName(pollInfoMap, list);
            } else if (AlarmDetailTypeEnum.ALARM_CONSTANT.code.equals(alarmType)) {
                messType = "【数据异常报警-恒值】 ";
                pollName = getPollName(pollInfoMap, list);
            } else if (AlarmDetailTypeEnum.ALARM_NEGATIVE.code.equals(alarmType)) {
                messType = "【数据异常报警-负值】 ";
                pollName = getPollName(pollInfoMap, list);
            } else if (AlarmDetailTypeEnum.ALARM_HOUR_IMPERFECT.code.equals(alarmType)) {
                messType = "【数据缺失报警-小时数据不完整】 ";
                StringBuilder bu = new StringBuilder();
                for (BaseDurAlarm info : list) {
                    for (String pollCode : info.getAlarmMsg().split(",")) {
                        if (pollInfoMap.containsKey(pollCode)) {
                            if (bu.length() > 0) {
                                bu.append("，");
                            }
                            bu.append(pollInfoMap.get(pollCode).getPollutantNameCn());
                        }
                    }
                }
                pollName = bu.toString();
            } else if (AlarmDetailTypeEnum.ALARM_LARGE.code.equals(alarmType)) {
                // 数据超标（超上限）
                if (DataTypeEnum.minute.code.equals(dataType)) {
                    messType = "【数据超标报警-分钟数据超标】 ";
                    pollName = getPollName(pollInfoMap, list);
                } else if (DataTypeEnum.hour.code.equals(dataType)) {
                    messType = "【数据超标报警-小时数据超标】 ";
                    pollName = getPollName(pollInfoMap, list);
                }
            }
            String timeStr;
            if (DataTypeEnum.minute.code.equals(dataType)) {
                timeStr = time.format(DateUtils.yy_m_d_h_m);
            } else if (DataTypeEnum.hour.code.equals(dataType)) {
                timeStr = time.format(DateUtils.yy_m_d_h);
            } else {
                timeStr = time.format(DateUtils.yy_m_d_h_m_s);
            }
            if (null != messType) {
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(outInfo.getEntCode())
                        .entName(outInfo.getEntName())
                        .outPutCode(outInfo.getOutPutCode())
                        .outPutName(outInfo.getOutPutName())
                        .outPutType(outInfo.getOutPutType())
                        .dataType(dataType)
                        .alarmType(alarmType)
                        .alarmTime(time)
                        .message(pollName)
                        .build());
                weComSend.sendWXMessage(token, outInfo.getWeComMsg(), messType + outInfo.getEntName() + " " + outInfo.getOutPutName() + "；" +
                        timeStr + " " + pollName + " 请您注意！");
            }
        }));
    }

    private void sendWXWarnMessage(List<BaseDurAlarm> warnList, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap) {
        if (warnList.isEmpty()) {
            return;
        }
        /* 发送预警消息 */
        Map<Integer, List<BaseDurAlarm>> alarmMap = warnList.stream().collect(Collectors.groupingBy(BaseDurAlarm::getAlarmType));
        String token = weComSend.getAccessToken();
        alarmMap.forEach( (k, v) -> {
            LocalDateTime time = v.get(0).getAlarmTime();
            String messType = null, pollName = null;
            if (AlarmDetailTypeEnum.WARN_ZERO.code.equals(k)) {
                messType = "【数据异常预警-零值】 ";
                pollName = getPollName(pollInfoMap, v);
            } else if (AlarmDetailTypeEnum.WARN_CONSTANT.code.equals(k)) {
                messType = "【数据异常预警-恒值】 ";
                pollName = getPollName(pollInfoMap, v);
            } else if (AlarmDetailTypeEnum.WARN_NEGATIVE.code.equals(k)) {
                messType = "【数据异常预警-负值】 ";
                pollName = getPollName(pollInfoMap, v);
            }
            String timeStr;
            if (DataTypeEnum.minute.code.equals(outInfo.getOutPutType())) {
                timeStr = time.format(DateUtils.yy_m_d_h_m);
            } else if (DataTypeEnum.hour.code.equals(outInfo.getOutPutType())) {
                timeStr = time.format(DateUtils.yy_m_d_h);
            } else {
                timeStr = time.format(DateUtils.yy_m_d_h_m_s);
            }
            if (null != messType) {
                // 预警
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(outInfo.getEntCode())
                        .entName(outInfo.getEntName())
                        .outPutCode(outInfo.getOutPutCode())
                        .outPutName(outInfo.getOutPutName())
                        .outPutType(outInfo.getOutPutType())
                        .dataType(DataTypeEnum.minute.code)
                        .alarmType(AlarmDetailTypeEnum.WARN_NEGATIVE.code)
                        .alarmTime(time)
                        .message(pollName)
                        .build());
                weComSend.sendWXMessage(token, outInfo.getWeComMsg(), messType + outInfo.getEntName() + " " + outInfo.getOutPutName() + "；" +
                        timeStr + " " + pollName + " 请您注意！");
            }
        });
    }

    private void sendPhoneMessage(List<BaseDurAlarm> alarmList, MqttMsgInfo info, OutPutInfo outInfo, Map<String, OutPutPollInfo> pollInfoMap) {
        if (alarmList.isEmpty()) {
            return;
        }
        /* 发送报警消息 */
        Map<Integer, List<BaseDurAlarm>> alarmMap = alarmList.stream().collect(Collectors.groupingBy(BaseDurAlarm::getAlarmType));
        alarmMap.forEach( (k, v) -> {
            LocalDateTime time = v.get(0).getAlarmTime();
            if (AlarmDetailTypeEnum.ALARM_LARGE.code.equals(k)) {
                if (OutPutTypeEnum.OUT_PUT_FS.code.equals(outInfo.getOutPutType()) && DataTypeEnum.day.code.equals(info.getDataTypeInt())) {
                    // 废水日数据超标机器人电话
                    aliYunVoicePush.textToVoice(outInfo.getPerAlarmPhone(), "您好，企业" + outInfo.getEntName() +
                            "下排口" + outInfo.getOutPutName() +
                            "的污染物"+ getPollName(pollInfoMap, v) +
                            "在"+ time.format(DateUtils.YMD_ZH)  + " 日数据超标，请您注意！");
                } else if (OutPutTypeEnum.OUT_PUT_FQ.code.equals(outInfo.getOutPutType())) {
                    // 废气小时数据超标机器人电话
                    aliYunVoicePush.textToVoice(outInfo.getPerAlarmPhone(), "您好，企业" + outInfo.getEntName() +
                            "下排口" + outInfo.getOutPutName() +
                            "的污染物"+ getPollName(pollInfoMap, v) +
                            "在"+ time.format(DateUtils.YMD_H_ZH)  + " 小时数据超标，请您注意！");
                }
            }
        });
    }

    private void addAlarmInfo(List<BaseDurAlarm> alarmList, String outPutId, String pollutantCode, Integer dataType,
                              Integer alarmType, LocalDateTime monitorTime, String alarmMsg) {
        if (null != alarmType) {
            BaseDurAlarm alarm = new BaseDurAlarm();
            alarm.setAlarmId(UlidCreator.getMonotonicUlid().toString());
            alarm.setOutPutId(outPutId);
            alarm.setPollutantCode(pollutantCode);
            alarm.setDataType(dataType);
            alarm.setAlarmType(alarmType);
            alarm.setAlarmTime(monitorTime);
            alarm.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
            alarm.setAlarmMsg(alarmMsg);
            alarmList.add(alarm);
        }
    }

    /**
     * 获取数据中的采样数据
     * 上报折算值采用折算值，无折算值用实测值
     */
    private BigDecimal getSampleValue(Map<String, Object> data) {
        String strValue = MapUtil.getStr(data, "zsAvg");
        if (StringUtils.isEmpty(strValue)) {
            strValue = MapUtil.getStr(data,"avg");
        }
        if (StringUtils.isEmpty(strValue)) {
            return null;
        }
        return new BigDecimal(strValue);
    }

    private LocalDateTime getPollCycleTime(JSONObject pollCycle, String key, LocalDateTime def) {
        LocalDateTime get = null;
        if (pollCycle.containsKey(key)) {
            get = DateUtils.strToLocalDateTime(pollCycle.getString(key), DateUtils.yy_m_d_h_m_s);
        }
        return null == get ? def : get;
    }
}

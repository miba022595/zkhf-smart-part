package com.zkhf.epmis.process.task.handler;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
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
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.base.BaseAlarmMapper;
import com.zkhf.epmis.process.mapper.task.TaskHandlerMapper;
import com.zkhf.epmis.process.send.weixin.WeComSend;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import com.zkhf.epmis.process.task.domain.PollDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 数据缺失检测
 */
@Component
public class DataLossCheckJobHandler {

    private TaskHandlerMapper taskHandlerMapper;
    @Autowired
    public void setTaskHandlerMapper(TaskHandlerMapper taskHandlerMapper) {
        this.taskHandlerMapper = taskHandlerMapper;
    }

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

    private SseMessagingService sseMessagingService;
    @Autowired
    public void setSseMessagingService(SseMessagingService sseMessagingService) {
        this.sseMessagingService = sseMessagingService;
    }

    private RedisCache redisCache;
    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    private RedisCacheUtils redisCacheUtils;

    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    /**
     * 小时缺失预警查询
     * 查询条件当前小时往前推2个小时
     * 报警数据为小时整点数据
     */
    @XxlJob("hourDataMissingJobHandler")
    public void hourDataMissingJobHandler() {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 查询开始时间
        LocalDateTime start = now.minusHours(2).truncatedTo(ChronoUnit.HOURS);
        // 查询结束时间
        LocalDateTime end = now.minusHours(1).truncatedTo(ChronoUnit.HOURS).minusSeconds(1);
        // 数据表名称组成：t_data_out_年份_排口id
        int year = DateUtils.getTableYear(start);
        String tableName = "t_data_out_" + year + "_";
        // 获取所有数据表名
        List<String> tableNames = taskHandlerMapper.selectTableName(tableName + "%");
        // 获取排口基础信息
        Map<String, OutPutInfo> outMap = new HashMap<>();
        // 获取设备与排口的关系
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        if (outList.isEmpty()) {
            XxlJobHelper.log("未查询到企业-排口-设备信息");
            return;
        }
        // 表名忽略了大小写
        outList.forEach( e -> outMap.put(e.getOutPutId().toLowerCase(), e));
        // 排口主键id
        String lowerOutId;
        int len = tableName.length();
        List<OutPutInfo> hourMissList = new ArrayList<>();
        for (String name : tableNames) {
            lowerOutId = name.substring(len);
            OutPutInfo out = outMap.get(lowerOutId);
            if (null == out) {
                XxlJobHelper.log("未找到对应的排口 {}", lowerOutId);
                continue;
            }
            // 只计算废气、废水的小时数据缺失报警
            if (!OutPutTypeEnum.OUT_PUT_FQ.code.equals(out.getOutPutType()) && !OutPutTypeEnum.OUT_PUT_FS.code.equals(out.getOutPutType())) {
                continue;
            }
            if (!OutPutStatusEnum.OUT_PUT_STATUS_ZC.code.equals(out.getOutPutStatus())) {
                XxlJobHelper.log("企业 {} 排口 {} 状态 {}", out.getEntName(), out.getOutPutName(), OutPutStatusEnum.getNameByCode(out.getOutPutStatus()));
                continue;
            }
            // 判断小时数据是否存在
            List<PollDataInfo> dataList = taskHandlerMapper.getOutPutDataList(name, DataTypeEnum.hour.code, start, end);
            if (null == dataList || dataList.isEmpty()) {
                // 登记小时数据缺失的排口
                hourMissList.add(out);
            }
        }
        updateAlarm(hourMissList, start);
    }

    private void updateAlarm(List<OutPutInfo> hourMissList, LocalDateTime start) {
        // 获取排口未解除的报警
        List<Map<String, String>> activeList = baseAlarmMapper.selectAlarmList(null, DataTypeEnum.hour.code, AlarmDetailTypeEnum.ALARM_HOUR_MISS.code, Constants.ALARM_STATUS_ACTIVE);
        Map<String, String> activeMap = new HashMap<>();
        activeList.forEach( e -> activeMap.put(MapUtil.getStr(e,"out_put_id"), MapUtil.getStr(e,"alarm_id")));
        List<OutPutInfo> insertList = new ArrayList<>();
        String cacheKey = CacheConstants.DATA_CACHE_KEY + "ent:data:alarmDataLossTime:" + DataTypeEnum.hour.code;
        JSONObject alarmTime = getCacheData(cacheKey);
        List<BaseDurAlarm> alarmList = new ArrayList<>();
        hourMissList.forEach( e -> {
            if (activeMap.containsKey(e.getOutPutId())) {
                activeMap.remove(e.getOutPutId());
            } else {
                BaseDurAlarm info = new BaseDurAlarm();
                info.setAlarmId(UlidCreator.getMonotonicUlid().toString());
                info.setOutPutId(e.getOutPutId());
                info.setPollutantCode("");
                info.setDataType(DataTypeEnum.hour.code);
                info.setAlarmType(AlarmDetailTypeEnum.ALARM_HOUR_MISS.code);
                info.setAlarmTime(start);
                info.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
                info.setAlarmMsg("");
                alarmList.add(info);
                insertList.add(e);
            }
            alarmTime.put(e.getOutPutId(), start.format(DateUtils.yy_m_d_h_m_s));
        });
        // 报警解除
        if (!activeMap.isEmpty()) {
            Map<String, String> updateMap = new HashMap<>();
            String nowStr = start.format(DateUtils.yy_m_d_h_m_s);
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
        if (!alarmList.isEmpty()) {
            baseAlarmMapper.batchInsertDurAlarm(alarmList);
            // 发送报警消息
            insertList.forEach( e -> {
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(e.getEntCode())
                        .entName(e.getEntName())
                        .outPutCode(e.getOutPutCode())
                        .outPutName(e.getOutPutName())
                        .outPutType(e.getOutPutType())
                        .dataType(DataTypeEnum.hour.code)
                        .alarmType(AlarmDetailTypeEnum.ALARM_HOUR_MISS.code)
                        .alarmTime(start)
                        .build());
                weComSend.sendWXMessage(e.getWeComMsg(), "【数据缺失预警-小时数据缺失】：" + e.getEntName() + "，" +
                        e.getOutPutName() + "；" + start.format(DateUtils.yy_m_d_h) + " 请您注意！");
            });
        }
    }

    private JSONObject getCacheData(String cacheKey) {
        JSONObject cache = redisCache.getCacheObject(cacheKey);
        if (null == cache) {
            cache = new JSONObject();
        }
        return cache;
    }

    /**
     * 联网异常预警--实时数据
     * 5分钟执行一次，离线判断依据报警配置判断
     */
    @XxlJob("lianWangYiChangYuJ")
    public void lianWangYiChangYuJ() {
        // 获取所有排口
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        if (outList.isEmpty()) {
            XxlJobHelper.log("未查询到企业-排口-设备信息");
            return;
        }
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // 数据表名称组成：t_data_out_年份，5316150条可以了)_排口id
        int year = DateUtils.getTableYear(now);
        String tableName = "t_data_out_" + year + "_";
        // 报警配置
        Map<String, Map<Integer, OutPutAlarmConf>> confMap = RedisCacheUtils.outPutAlarmConfMap;
        // 获取所有数据表名
        List<String> tableNames = taskHandlerMapper.selectTableName(tableName + "%");
        Map<String, OutPutInfo> outMap = new HashMap<>();
        // 表名忽略了大小写
        List<String> onlineList = new ArrayList<>();
        List<BaseDurAlarm> warnList = new ArrayList<>();
        List<BaseDurAlarm> alarmList = new ArrayList<>();
        // 联网异常预警列表
        LocalDateTime time30 = now.minusMinutes(30), timeA, timeW;
        // 离线判断时间
        int len = 30;
        Integer confLen;
        for (OutPutInfo e : outList) {
            timeA = null;
            timeW = null;
            String item = tableName + e.getOutPutId().toLowerCase();
            if (tableNames.contains(item)) {
                outMap.put(e.getOutPutId(), e);
                // 联网异常报警判断时间
                confLen = getConfLen(confMap, e.getOutPutId(), AlarmDetailTypeEnum.ALARM_NET_ERR.code);
                if (null != confLen && len < confLen) {
                    len = confLen;
                    timeA = now.minusMinutes(confLen);
                }
                // 联网异常预警判断时间
                confLen = getConfLen(confMap, e.getOutPutId(), AlarmDetailTypeEnum.WARN_NET_ERR.code);
                if (null != confLen && len < confLen) {
                    len = confLen;
                    timeW = now.minusMinutes(confLen);
                }
                // 获取实时数据最新时间
                LocalDateTime maxTime = taskHandlerMapper.getNewestDate(item, DataTypeEnum.real.code, now.minusMinutes(len));
                if (null != maxTime && maxTime.isAfter(time30)) {
                    onlineList.add(e.getOutPutId());
                }
                if (null != timeA && null != maxTime && maxTime.isBefore(timeA)) {
                    // 登记联网异常报警
                    BaseDurAlarm durAlarm = new BaseDurAlarm();
                    durAlarm.setAlarmId(UlidCreator.getMonotonicUlid().toString());
                    durAlarm.setOutPutId(e.getOutPutId());
                    durAlarm.setDataType(DataTypeEnum.real.code);
                    durAlarm.setAlarmType(AlarmDetailTypeEnum.ALARM_NET_ERR.code);
                    durAlarm.setAlarmTime(now);
                    durAlarm.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
                    alarmList.add(durAlarm);
                    continue;
                }
                if (null != timeW && null != maxTime && maxTime.isBefore(timeW)) {
                    // 登记联网异常预警
                    BaseDurAlarm durAlarm = new BaseDurAlarm();
                    durAlarm.setAlarmId(UlidCreator.getMonotonicUlid().toString());
                    durAlarm.setOutPutId(e.getOutPutId());
                    durAlarm.setDataType(DataTypeEnum.real.code);
                    durAlarm.setAlarmType(AlarmDetailTypeEnum.WARN_NET_ERR.code);
                    durAlarm.setAlarmTime(now);
                    durAlarm.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
                    warnList.add(durAlarm);
                }
            }
        }
        // 更新报警信息
        updateAlarm(alarmList, outMap, now);
        // 更新预警信息
        updateWarn(warnList, outMap);
        // 设置缓存，在线列表
        redisCacheUtils.setAllOutPutOnlineList(onlineList, 40);
    }

    private Integer getConfLen(Map<String, Map<Integer, OutPutAlarmConf>> confMap, String outPutId, Integer alarmType) {
        Map<Integer, OutPutAlarmConf> typeMap = confMap.get(outPutId);
        if (null == typeMap) {
            return null;
        }
        OutPutAlarmConf conf = typeMap.get(alarmType);
        if (null != conf && Constants.ALARM_CONF_ON.equals(conf.getIsEnabled())) {
            return conf.getDataDur();
        }
        return null;
    }

    private void updateAlarm(List<BaseDurAlarm> alarmList, Map<String, OutPutInfo> outMap, LocalDateTime date) {
        // 获取排口未解除的报警
        List<Map<String, String>> activeList = baseAlarmMapper.selectAlarmList(null, DataTypeEnum.real.code, AlarmDetailTypeEnum.ALARM_NET_ERR.code,
                Constants.ALARM_STATUS_ACTIVE);
        Map<String, String> activeMap = new HashMap<>();
        activeList.forEach( e -> activeMap.put(MapUtil.getStr(e,"out_put_id"), MapUtil.getStr(e,"alarm_id")));
        String cacheKey = CacheConstants.DATA_CACHE_KEY + "ent:data:alarmDataLossTime:" + DataTypeEnum.real.code;
        JSONObject alarmTime = getCacheData(cacheKey);
        List<BaseDurAlarm> insertList = new ArrayList<>();
        for (BaseDurAlarm e : alarmList) {
            if (activeMap.containsKey(e.getOutPutId())) {
                activeMap.remove(e.getOutPutId());
            } else {
                insertList.add(e);
            }
            alarmTime.put(e.getOutPutId(), e.getAlarmTime().format(DateUtils.yy_m_d_h_m_s));
        }
        // 报警解除
        if (!activeMap.isEmpty()) {
            Map<String, String> updateMap = new HashMap<>();
            String nowStr = date.format(DateUtils.yy_m_d_h_m_s);
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
            /* 发送报警消息 */
            insertList.forEach( e -> {
                OutPutInfo outInfo = outMap.get(e.getOutPutId());
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(outInfo.getEntCode())
                        .entName(outInfo.getEntName())
                        .outPutCode(outInfo.getOutPutCode())
                        .outPutName(outInfo.getOutPutName())
                        .outPutType(outInfo.getOutPutType())
                        .dataType(e.getDataType())
                        .alarmType(e.getAlarmType())
                        .alarmTime(e.getAlarmTime())
                        .build());
                weComSend.sendWXMessage(outInfo.getWeComMsg(), "【数据异常报警-联网异常】："
                        + outInfo.getEntName() + " " + outInfo.getOutPutName() + " " + e.getAlarmTime().format(DateUtils.yy_m_d_h_m_s)
                        + " 请您注意！");
            });
        }
    }

    private void updateWarn(List<BaseDurAlarm> warnList, Map<String, OutPutInfo> outMap) {
        // 删除旧的预警信息
        baseAlarmMapper.deleteOldWarn(null, DataTypeEnum.real.code,
                Collections.singletonList(AlarmDetailTypeEnum.WARN_NET_ERR.code));
        if (warnList.isEmpty()) {
            return;
        }
        // 插入或更新报警信息
        baseAlarmMapper.batchInsertWarn(warnList);
        warnList.forEach( e -> {
            OutPutInfo out = outMap.get(e.getOutPutId());
            // 发送联网异常预警
            sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                    .entCode(out.getEntCode())
                    .entName(out.getEntName())
                    .outPutCode(out.getOutPutCode())
                    .outPutName(out.getOutPutName())
                    .outPutType(out.getOutPutType())
                    .dataType(e.getDataType())
                    .alarmType(e.getAlarmType())
                    .alarmTime(e.getAlarmTime())
                    .build());
            weComSend.sendWXMessage(out.getWeComMsg(), "【数据异常预警-联网异常】："
                    + out.getEntName() + " " + out.getOutPutName() + " " + e.getAlarmTime().format(DateUtils.yy_m_d_h_m_s)
                    + " 请您注意！");
        });
    }
}

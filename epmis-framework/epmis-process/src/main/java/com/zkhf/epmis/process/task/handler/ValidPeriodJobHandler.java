package com.zkhf.epmis.process.task.handler;

import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.constant.CacheConstants;
import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import com.zkhf.epmis.core.enums.AlarmTypeEnum;
import com.zkhf.epmis.core.enums.ValidPeriodTypeEnum;
import com.zkhf.epmis.core.redis.RedisCache;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.mapper.task.ValidPeriodTaskMapper;
import com.zkhf.epmis.process.send.weixin.WeComSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 各类资质数据有效期任务
 */
@Component
public class ValidPeriodJobHandler {

    private final String validPeriodDataKey = CacheConstants.DATA_CACHE_KEY + "ent:data:validPeriod";

    private ValidPeriodTaskMapper validPeriodTaskMapper;
    @Autowired
    public void setValidPeriodTaskMapper(ValidPeriodTaskMapper validPeriodTaskMapper) {
        this.validPeriodTaskMapper = validPeriodTaskMapper;
    }

    private WeComSend weComSend;

    @Autowired
    public void setWeComSend(WeComSend weComSend) {
        this.weComSend = weComSend;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    private RedisCache redisCache;
    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 各类资质数据有效期任务，每小时查询一次
     */
    @XxlJob("validPeriodJobTask")
    public void validPeriodJobTask() {
        // 获取所有资质数据有效期列表
        List<ValidPeriodAlarmInfo> list = platformFacade.validPeriodConfList();
        if (null == list) {
            XxlJobHelper.log("未查询到资质数据有效期列表");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        // 判断是否要推送报警
        Map<String, LocalDateTime> lastSendMap = getLastSendMap();
        Map<String, ValidPeriodAlarmInfo> sendMap = new HashMap<>();
        boolean send;
        for (ValidPeriodAlarmInfo info : list) {
            if(StringUtils.isEmpty(info.getWeComMsg())) { // 未配置关联企业微信信息
                XxlJobHelper.log("未配置关联企业微信信息 {}", info.getEntName());
                continue;
            }
            if (AlarmTypeEnum.notContainsCode(info.getAlarmType())) { // 未知的报警类型
                XxlJobHelper.log("未知的报警类型 {}", info.getAlarmType());
                continue;
            }
            if (null == info.getAlarmRage()) { // 未知的报警频次
                continue;
            }
            String rageType = info.getAlarmRage().substring(0, 1);
            int rageTimes = StringUtils.strToInt(info.getAlarmRage().substring(1), -1);
            if (rageTimes < 1) {
                continue;
            }
            // 获取配置类型
            ChronoUnit unit = "M".equals(rageType) ? ChronoUnit.MONTHS : "D".equals(rageType) ? ChronoUnit.DAYS : "H".equals(rageType) ? ChronoUnit.HOURS : null;
            if (null == unit) { // 未知的校验类型
                continue;
            }
            String key = info.getEntCode() + "_" + info.getConfType() + "_" + info.getItemId();
            send = false;
            if (lastSendMap.containsKey(key)) { // 上次发送过，判断发送间隔
                long monthsBetween = unit.between(lastSendMap.get(key), now);
                if (monthsBetween >= rageTimes) { // 超过上次发送的间隔了，可以再次发送
                    send = true;
                }
            } else { // 上次未发送，直接发送
                send = true;
            }
            if (send) {
                info.setLastSendTime(now);
                sendMap.put(key, info);
                lastSendMap.put(key, now);
            }
        }
        /* 发送消息 */
        if (!sendMap.isEmpty()) {
            // 获取token，减少多次获取
            String token = weComSend.getAccessToken();
            sendMap.values().forEach( e -> {
                StringBuilder msgBu = new StringBuilder();
                if (AlarmTypeEnum.ALARM_RED.code.equals(e.getAlarmType())) {
                    msgBu.append("红色过期报警】：").append(e.getEntName());
                    msgBu.append("-").append(e.getConfDesc());
                    if (StringUtils.isNotEmpty(e.getItemName())) {
                        msgBu.append("-").append(e.getItemName());
                    }
                    if (e.getLeftDays() > 0) {
                        msgBu.append(" 剩余天数").append(e.getLeftDays()).append("；请立即");
                    } else {
                        msgBu.append(" 已过期，请立即");
                    }
                } else if (AlarmTypeEnum.ALARM_ORANGE.code.equals(e.getAlarmType())) {
                    msgBu.append("橙色过期预警】：").append(e.getEntName());
                    msgBu.append("-").append(e.getConfDesc());
                    if (StringUtils.isNotEmpty(e.getItemName())) {
                        msgBu.append("-").append(e.getItemName());
                    }
                    msgBu.append(" 剩余天数").append(e.getLeftDays()).append("；请及时");
                } else if (AlarmTypeEnum.ALARM_YELLOW.code.equals(e.getAlarmType())) {
                    msgBu.append("黄色过期提醒】：").append(e.getEntName());
                    msgBu.append("-").append(e.getConfDesc());
                    if (StringUtils.isNotEmpty(e.getItemName())) {
                        msgBu.append("-").append(e.getItemName());
                    }
                    msgBu.append(" 剩余天数").append(e.getLeftDays()).append("；请及时");
                }
                if (msgBu.length() > 0) {
                    String msgStart;
                    if (ValidPeriodTypeEnum.J_C_R_W.code.equals(e.getConfType())) {
                        msgStart = "【手工监测任务";
                    } else {
                        msgStart = "【资质";
                    }
                    String msgEnd;
                    if (ValidPeriodTypeEnum.J_C_R_W.code.equals(e.getConfType())) {
                        msgEnd = "上报!";
                    } else {
                        msgEnd = "更新!";
                    }
                    weComSend.sendWXMessage(token, e.getWeComMsg(), msgStart + msgBu + msgEnd);
                }
            });
        }
        // 更新缓存
        setLastSendMap(lastSendMap);
        // 清空有效期数据
        validPeriodTaskMapper.truncateValidPeriodInfo();
        if (!list.isEmpty()) { // 插入有效期数据
            validPeriodTaskMapper.saveValidPeriodInfoList(list);
        }
    }

    private Map<String, LocalDateTime> getLastSendMap() {
        // 读取
        Object data = redisCache.getCacheObject(validPeriodDataKey);
        Map<String, LocalDateTime> map = new HashMap<>();
        if (null == data) {
            return map;
        }
        try {
            ((JSONObject)data).forEach((key, value) -> map.put(key, LocalDateTime.parse(value.toString(), DateUtils.iso_dtf)));
        } catch (Exception ignore) {
            XxlJobHelper.log("缓存数据转换失败 {}", validPeriodDataKey);
        }
        return map;
    }

    private void setLastSendMap(Map<String, LocalDateTime> cacheTime) {
        JSONObject json = new JSONObject();
        cacheTime.forEach((key, value) -> json.put(key, value.format(DateUtils.iso_dtf)));
        // 更新缓存
        redisCache.setCacheObject(validPeriodDataKey, json);
    }
}

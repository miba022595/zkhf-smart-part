package com.zkhf.epmis.process.sse.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.sse.domain.SseEmitterInfo;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import com.zkhf.epmis.process.sse.domain.SubscribeEnum;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseMessagingServiceImpl implements SseMessagingService {

    /** 重连时间 */
    @Value("${sse.reconnectTime:5000}")
    private int reconnectTime;
    /** 最大空闲时间。先配置10分钟 */
    @Value("${sse.maxFeeTime:600000}")
    private int maxFeeTime;

    // 保存所有连接的客户端
    private final Map<String, SseEmitterInfo> allEmitter = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String eventType, String clientId, String otherParam) {
        // 设置0表示不超时，或者设置合理的超时时间(毫秒)
        SseEmitter emitter = new SseEmitter(0L);
        List<String> authList;
        if (GVarContainer.isNotAdmin()) {
            authList = GVarContainer.getEntCodes();
        } else {
            // 管理员用户设置-1
            authList = SseEmitterInfo.Admin_ent_list;
        }
        // 将新的emitter添加到map中，旧的设置完成
        String key = eventType + "_" + clientId;
        // 获取后判断，防止删除时迭代问题
        SseEmitterInfo info = allEmitter.get(key);
        if (null != info) {
            info.getSseEmitter().complete();
        }
        info = SseEmitterInfo.builder()
                .eventType(eventType)
                .clientId(clientId)
                .otherParam(otherParam)
                .authList(authList)
                .sseEmitter(emitter)
                .build();
        allEmitter.put(key, info);

        // 设置回调，当客户端断开时从map中移除
        emitter.onCompletion(() -> {
            log.error("client onCompletion eventType {}, clientId {}", eventType, clientId);
            allEmitter.remove(key);
        });

        emitter.onTimeout(() -> {
            log.error("client onTimeout eventType {}, clientId {}", eventType, clientId);
            emitter.complete();
            allEmitter.remove(key);
        });

        // 发送初始连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name(SubscribeEnum.connected.name())
                    .data("连接成功 " + clientId)
                    .reconnectTime(reconnectTime));
            info.setLastSendTime(System.currentTimeMillis());
        } catch (IOException e) {
            log.error("client completeWithError eventType {}, clientId {}", eventType, clientId, e);
            emitter.completeWithError(e);
            allEmitter.remove(key);
        }
        return emitter;
    }

    @Async
    @Override
    public void notifyBroadcast(String eventType, String message) {
        if (StringUtils.isEmpty(eventType)) {
            return;
        }
        Iterator<SseEmitterInfo> iter = allEmitter.values().iterator();;
        while (iter.hasNext()) {
            SseEmitterInfo info = iter.next();
            if (!eventType.equals(info.getEventType())) {
                continue;
            }
            sendToClient(iter, info, message);
        }
    }

    @Async
    @Override
    public void notifyBroadcast(String eventType, String entCode, String message) {
        if (StringUtils.isEmpty(eventType) || StringUtils.isEmpty(entCode)) {
            return;
        }
        Iterator<SseEmitterInfo> iter = allEmitter.values().iterator();
        while (iter.hasNext()) {
            SseEmitterInfo info = iter.next();
            if (!eventType.equals(info.getEventType())) {
                continue;
            }
            List<String> authList = info.getAuthList();
            if (null == authList) {
                continue;
            }
            // 判断非admin账号时权限包含
            if (!(authList.size() == 1 && SseEmitterInfo.Admin_ent.equals(authList.get(0))) && !authList.contains(entCode)) {
                continue;
            }
            sendToClient(iter, info, message);
        }
    }

    @Async
    @Override
    public void notifyBroadcastAlarmInfo(SseOnlineAlarm alarm) {
        alarm.setOutPutTypeDesc(OutPutTypeEnum.getNameByCode(alarm.getOutPutType()));
        alarm.setDataTypeDesc(DataTypeEnum.getNameByCode(alarm.getDataType()));
        alarm.setAlarmTypeDesc(AlarmDetailTypeEnum.getNameByCode(alarm.getAlarmType()).name);

        Iterator<SseEmitterInfo> iter = allEmitter.values().iterator();
        while (iter.hasNext()) {
            SseEmitterInfo info = iter.next();
            if (!SubscribeEnum.onlineDataAlarm.name().equals(info.getEventType())) {
                continue;
            }
            List<String> authList = info.getAuthList();
            if (null == authList) {
                continue;
            }
            // 判断非admin账号时权限包含
            if (!(authList.size() == 1 && SseEmitterInfo.Admin_ent.equals(authList.get(0))) && !authList.contains(alarm.getEntCode())) {
                continue;
            }
            // 判断报警是否订阅
            if (!containsNumber(info.getOtherParam(), alarm.getAlarmType())) {
                continue;
            }
            sendToClient(iter, info, JSONObject.toJSONString(alarm));
        }
    }

    private boolean containsNumber(String alarmStr, Integer alarmCode) {
        if (alarmStr == null || alarmStr.isEmpty()) {
            return false;
        }
        String targetStr = String.valueOf(alarmCode);
        // 特殊情况：整个字符串就是目标数字
        if (alarmStr.equals(targetStr)) {
            return true;
        }
        // 在开头的情况
        if (alarmStr.startsWith(targetStr + ",")) {
            return true;
        }
        // 在中间的情况
        if (alarmStr.contains("," + targetStr + ",")) {
            return true;
        }
        // 在结尾的情况
        return alarmStr.endsWith("," + targetStr);
    }

    private void sendToClient(Iterator<SseEmitterInfo> iter, SseEmitterInfo info, String message) {
        try {
            info.getSseEmitter().send(SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name(info.getEventType())
                    .data(message));
            info.setLastSendTime(System.currentTimeMillis());
        } catch (IOException e) {
            log.error("notifyBroadcast IOException clientId {}, eventType {}", info.getClientId(), info.getEventType(), e);
            info.getSseEmitter().complete();
            // 使用迭代器安全删除
            iter.remove();
        }
    }

    @Override
    public AjaxResult unsubscribe(String eventType, String clientId) {
        // 命中时迭代删除
        Iterator<SseEmitterInfo> iter = allEmitter.values().iterator();;
        while (iter.hasNext()) {
            SseEmitterInfo info = iter.next();
            if ((StringUtils.isEmpty(eventType) || eventType.equals(info.getEventType()))
                    && clientId.equals(info.getClientId())) {
                info.getSseEmitter().complete();
                iter.remove();
            }
        }
        return AjaxResult.success("success");
    }

    @Override
    public AjaxResult getSubscriptionStatus(String clientId) {
        List<SseEmitterInfo> list = new ArrayList<>();
        AjaxResult result = AjaxResult.success(list);
        if (StringUtils.isEmpty(clientId)) {
            return result;
        }
        for (SseEmitterInfo info : allEmitter.values()) {
            if (clientId.equals(info.getClientId())) {
                list.add(info);
            }
        }
        return result;
    }

    @Override
    public void cleanupInactiveConnection() {
        log.info("开始清理失效的SSE连接，总连接数 {}", allEmitter.size());
        long now = System.currentTimeMillis();
        int cleanedCount = 0;
        Iterator<SseEmitterInfo> iter = allEmitter.values().iterator();
        while (iter.hasNext()) {
            SseEmitterInfo info = iter.next();
            // 超过配置的最大空闲时间，则认为是无效的，移除
            if (now - info.getLastSendTime() > maxFeeTime) {
                info.getSseEmitter().complete();
                iter.remove();
                cleanedCount++;
            }
        }
        log.info("SSE连接清理完成，本次清理数量: {}", cleanedCount);
    }
}

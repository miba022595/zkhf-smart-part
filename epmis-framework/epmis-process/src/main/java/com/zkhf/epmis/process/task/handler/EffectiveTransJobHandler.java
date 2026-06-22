package com.zkhf.epmis.process.task.handler;

import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.OutPutPollInfo;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.task.TaskHandlerMapper;
import com.zkhf.epmis.process.send.weixin.WeComSend;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import com.zkhf.epmis.process.task.domain.EffectiveTransTask;
import com.zkhf.epmis.process.task.domain.PollDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 传输有效率任务
 */
@Component
public class EffectiveTransJobHandler {

    private TaskHandlerMapper taskHandlerMapper;
    @Autowired
    public void setTaskHandlerMapper(TaskHandlerMapper taskHandlerMapper) {
        this.taskHandlerMapper = taskHandlerMapper;
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

    private RedisCacheUtils redisCacheUtils;

    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    /**
     * 废气、废水自动监测数据传输率--每小时统计上个小时的小时数据，每天凌晨1点查统计昨天的日数据
     * 参考《自动监测数据有效传输率统计算法说明.pdf》文档
     * 小时、日数据
     */
    @XxlJob("dataTransRateTaskHandler")
    public void dataTransRateTaskHandler() {
        // 获取排口信息
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        if (outPutList.isEmpty()) {
            XxlJobHelper.log("未查询到排口污染物信息");
            return;
        }
        // 表名忽略了大小写
        Map<String, OutPutInfo> outPutMap = new HashMap<>();
        outPutList.forEach( e -> {
            // 只要废气、废水的排口
            if (OutPutTypeEnum.OUT_PUT_FQ.code.equals(e.getOutPutType()) || OutPutTypeEnum.OUT_PUT_FS.code.equals(e.getOutPutType())) {
                outPutMap.put(e.getOutPutId().toLowerCase(), e);
            }
        });
        // 获取排口污染物信息
        List<OutPutPollInfo> outPollList = redisCacheUtils.getAllOutPutPollList(OutPutTypeEnum.OUT_PUT_FQ.code);
        // 获取废气传输率校验字段列表
        Map<String, Map<String, List<String>>> outPollFieldMap = new HashMap<>();
        outPollList.forEach( e -> {
            // 只要废气、废水的排口
            if ((OutPutTypeEnum.OUT_PUT_FQ.code.equals(e.getOutPutType()) || OutPutTypeEnum.OUT_PUT_FS.code.equals(e.getOutPutType()))
                    && StringUtils.isNotEmpty(e.getMonFactor())) {
                outPollFieldMap.computeIfAbsent(e.getOutPutId().toLowerCase(), k -> new HashMap<>()).
                        put(e.getPollutantCode(), Arrays.asList(e.getMonFactor().split(",")));
            }
        });
        // 结果数据列表
        List<EffectiveTransTask> effList = new ArrayList<>();
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 查询开始时间
        LocalDateTime start = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);
        // 查询结束时间
        LocalDateTime end = now.truncatedTo(ChronoUnit.HOURS).minusMinutes(10);
        // 数据表名称组成：t_data_out_年份_排口id
        int nowYear = DateUtils.getTableYear(start);
        String tableName = "t_data_out_" + nowYear + "_";
        // 获取所有数据表名
        List<String> tableNames = taskHandlerMapper.selectTableName(tableName + "%");
        // 计算小时数据
        effectiveTransList(effList, start, end, tableNames, outPutMap, outPollFieldMap, tableName.length(), DataTypeEnum.hour.code);
        // 当前小时是凌晨1时，执行昨天的日数据统计
        if (1 == now.getHour()) {
            // 查询开始时间
            start = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
            // 查询结束时间
            end = now.truncatedTo(ChronoUnit.DAYS).minusMinutes(10);
            // 判断昨天是否分表的节点
            int year = DateUtils.getTableYear(start);
            if (year != nowYear) {
                tableName = "t_data_out_" + year + "_";
                // 获取所有数据表名
                tableNames = taskHandlerMapper.selectTableName(tableName + "%");
            }
            // 计算日数据
            effectiveTransList(effList, start, end, tableNames, outPutMap, outPollFieldMap, tableName.length(), DataTypeEnum.day.code);
        }
        // 批量插入或更新数据
        if (!effList.isEmpty()) {
            taskHandlerMapper.batchInsertOrUpdateEffTrans(effList);
        }
    }

    private void effectiveTransList(List<EffectiveTransTask> effList, LocalDateTime start, LocalDateTime end, List<String> tableNames,
                                    Map<String, OutPutInfo> outPutMap, Map<String, Map<String, List<String>>> outPollFieldMap, int len, Integer dataType) {
        if (null == tableNames || tableNames.isEmpty()) {
            return;
        }
        if (null == effList) {
            return;
        }
        String timeStr = start.format(DateUtils.yymdhms);
        // 排口主键id
        String lowerOutId;
        for (String name : tableNames) {
            lowerOutId = name.substring(len);
            OutPutInfo outPut = outPutMap.get(lowerOutId);
            if (null == outPut) {
                XxlJobHelper.log("未找到对应的排口 {}", lowerOutId);
                continue;
            }
            if (!Constants.OUT_PUT_STATUS_RUN.equals(outPut.getOutPutStatus())) {
                XxlJobHelper.log("企业 {} 排口 {} 未运行 ", outPut.getEntName(), outPut.getOutPutName());
                continue;
            }
            Map<String, List<String>> fieldMap = outPollFieldMap.get(lowerOutId);
            if (null == fieldMap) {
                XxlJobHelper.log("企业 {} 排口 {} 未勾选污染物检测数据 ", outPut.getEntName(), outPut.getOutPutName());
                continue;
            }
            EffectiveTransTask eff = new EffectiveTransTask();
            // 使用String.format确保3位数字，不足补零
            eff.setEffId(timeStr + String.format("%03d", dataType));
            eff.setOutPutId(outPut.getOutPutId());
            eff.setDataType(dataType);
            eff.setMonitorTime(start);
            // 应收
            eff.setMustTrans(fieldMap.values().stream().mapToInt(List::size).sum());
            // 实收
            eff.setRealTrans(0);
            // 获取数据
            List<PollDataInfo> dataList = taskHandlerMapper.getOutPutDataList(name, dataType, start, end);
            for (PollDataInfo data : dataList) {
                try {
                    // {"a00000": {"avg": "6.34", "cou": "3806.33", "max": "6.99", "min": "5.12"}
                    JSONObject info = JSONObject.parseObject(data.getDataInfo());
                    for (String pollCode : info.keySet()) {
                        List<String> fieldList = fieldMap.get(pollCode);
                        if (null == fieldList) {
                            continue;
                        }
                        Map<String, Object> obj = info.getJSONObject(pollCode);
                        if (null == obj || obj.isEmpty()) {
                            continue;
                        }
                        fieldList.forEach( e -> {
                            Object d = obj.get(e);
                            if (null != d) {
                                eff.setRealTrans(eff.getRealTrans() + 1);
                            }
                        });
                    }
                } catch (Exception ignore) {
                }
            }
            if (null != outPut.getTransRate()) {
                float rate = Math.round(eff.getRealTrans() * 10000.0 / eff.getMustTrans()) / 100.0f;;
                if (Float.compare(outPut.getTransRate(), rate) > 0) { // 小于指定传输率时推送报警
                    String message = null;
                    if (DataTypeEnum.hour.code.equals(dataType)) {
                        message = "【数据缺失报警-小时数据传输率】：" +
                                outPut.getEntName() + "，" + outPut.getOutPutName() + "；" +
                                rate +
                                "% 请您注意！";
                    } else if (DataTypeEnum.day.code.equals(dataType)) {
                        message = "【数据缺失报警-日数据传输率】：" +
                                outPut.getEntName() + "，" + outPut.getOutPutName() + "；" +
                                rate +
                                "% 请您注意！";
                    }
                    if (null != message) {
                        sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                                .entCode(outPut.getEntCode())
                                .entName(outPut.getEntName())
                                .outPutCode(outPut.getOutPutCode())
                                .outPutName(outPut.getOutPutName())
                                .outPutType(outPut.getOutPutType())
                                .dataType(dataType)
                                .alarmType(AlarmDetailTypeEnum.ALARM_HOUR_MISS.code)
                                .alarmTime(end)
                                .message(rate + "")
                                .build());
                        weComSend.sendWXMessage(outPut.getWeComMsg(), message);
                    }
                }
            }
            effList.add(eff);
        }
    }
}

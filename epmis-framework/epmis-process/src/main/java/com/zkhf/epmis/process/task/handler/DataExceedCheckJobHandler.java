package com.zkhf.epmis.process.task.handler;

import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.BaseDurAlarm;
import com.zkhf.epmis.process.base.domain.OutPutAlarmConf;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.OutPutPollInfo;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.base.BaseAlarmMapper;
import com.zkhf.epmis.process.mapper.task.TaskHandlerMapper;
import com.zkhf.epmis.process.send.weixin.WeComSend;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import com.zkhf.epmis.process.task.domain.DataHourLeftEmissionInfo;
import com.zkhf.epmis.process.task.domain.PollDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 数据超标检测
 */
@Component
public class DataExceedCheckJobHandler {

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

    private RedisCacheUtils redisCacheUtils;

    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    /**
     * 小时剩余控值
     * 查询分钟表数据，每5分钟查询一次
     */
    @XxlJob("hourDataLeftEmissionJobHandler")
    public void hourDataLeftEmissionJobHandler() {
        // 当前时间
        LocalDateTime date = LocalDateTime.now();
        // 获取查询开始时间（当前小时）
        LocalDateTime start = date.truncatedTo(ChronoUnit.HOURS);
        // 数据表名称组成：t_data_out_年份_排口id
        int year = DateUtils.getTableYear(date);
        String tableName = "t_data_out_" + year + "_";
        // 获取所有数据表名
        List<String> tableNames = taskHandlerMapper.selectTableName(tableName + "%");
        // 获取排口基础信息
        Map<String, OutPutInfo> outMap = new HashMap<>();
        // 获取设备mn号与排口的关系
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        if (outList.isEmpty()) {
            XxlJobHelper.log("未查询到企业-排口-设备信息");
            return;
        }
        // 获取当前小时已过分钟数用来计算当前小时分钟数据剩余接收条数
        int nowMinute = date.getMinute();
        // 表名忽略了大小写
        outList.forEach( e -> outMap.put(e.getOutPutId().toLowerCase(), e));
        // 获取排口污染物信息
        List<OutPutPollInfo> pollInfos = redisCacheUtils.getAllOutPutPollList();
        Map<String, Map<String, OutPutPollInfo>> outPollMap = new HashMap<>();
        pollInfos.forEach( e -> {
            Map<String, OutPutPollInfo> map;
            String key = e.getOutPutId().toLowerCase();
            if (outPollMap.containsKey(key)) {
                map = outPollMap.get(key);
            } else {
                map = new HashMap<>();
                outPollMap.put(key, map);
            }
            map.put(e.getPollutantCode(), e);
        });
        // 排口主键id
        String lowerOutId;
        int len = tableName.length();
        // 登记小时剩余排放列表
        List<DataHourLeftEmissionInfo> leftList = new ArrayList<>();
        // 登记废气小时数据超标预警列表
        List<BaseDurAlarm> warnList = new ArrayList<>();
        // 报警配置
        Map<String, Map<Integer, OutPutAlarmConf>> confMap = RedisCacheUtils.outPutAlarmConfMap;
        OutPutAlarmConf alarmConf;
        for (String name : tableNames) {
            lowerOutId = name.substring(len);
            OutPutInfo out = outMap.get(lowerOutId);
            if (null == out) {
                XxlJobHelper.log("未找到对应的排口 {}", lowerOutId);
                continue;
            }
            Map<Integer, OutPutAlarmConf> alarmConfMap = confMap.get(out.getOutPutId());
            boolean alarmEnabled = false;
            if (null == alarmConfMap) {
                XxlJobHelper.log("未配置报警配置 {}", lowerOutId);
            } else {
                alarmConf = alarmConfMap.get(AlarmDetailTypeEnum.WARN_LARGE.code);
                if (null == alarmConf
                        || !Constants.ALARM_CONF_ON.equals(alarmConf.getIsEnabled())
                        || !alarmConf.getDataType().contains(DataTypeEnum.getCodeStr(DataTypeEnum.minute.code))
                        || !alarmConf.getOutPutStatus().contains(OutPutStatusEnum.getCodeStr(out.getOutPutStatus()))) {
                    XxlJobHelper.log("上限预警未开启或排口状态、数据类型未匹配 {}", lowerOutId);
                } else {
                    alarmEnabled = true;
                }
            }
            Map<String, OutPutPollInfo> outPoll = outPollMap.get(lowerOutId);
            if (null == outPoll || outPoll.isEmpty()) {
                XxlJobHelper.log("企业 {} 排口 {} 未勾选污染物信息", out.getEntName(), out.getOutPutName());
                continue;
            }
            // 获取分钟数据列表
            List<PollDataInfo> dataList = taskHandlerMapper.getOutPutDataList(name, DataTypeEnum.minute.code, start, date);
            if (null == dataList || dataList.isEmpty()) {
                continue;
            }
            PollDataInfo last = dataList.get(0);
            Map<String, BigDecimal> total = new HashMap<>(); // 总量map
            int num = 0;
            for (PollDataInfo val : dataList) {
                if (StringUtils.isEmpty(val.getDataInfo())) {
                    continue;
                }
                num ++;
                JSONObject info = JSONObject.parseObject(val.getDataInfo());
                for (String pollCode : info.keySet()) {
                    BigDecimal avg = info.getJSONObject(pollCode).getBigDecimal("avg");
                    if (!total.containsKey(pollCode)) {
                        total.put(pollCode, avg);
                    } else {
                        total.put(pollCode, avg.add(total.get(pollCode)));
                    }
                }
            }
            BigDecimal size = new BigDecimal(num);
            BigDecimal minuteMonitorTotal = null;
            BigDecimal minuteSurplusCount = null;
            Integer minuteDataInterval = out.getMinuteDataInterval();
            if (null != minuteDataInterval && minuteDataInterval > 0) {
                // 计算每条数据的间隔分钟数 = 60分钟/分钟数据间隔；
                minuteMonitorTotal = new BigDecimal(60 / minuteDataInterval);
                // 计算当前小时剩余分钟数据接收条数 = 上一个小时的完整分钟数据条数-（当前已过分钟数/每条数据间隔分钟数）
                minuteSurplusCount = minuteMonitorTotal.subtract(new BigDecimal(nowMinute / minuteDataInterval));
            }
            // 根据当前污染物的标准值和当前小时的累计均值计算是否超出排放
            StringBuilder bu = new StringBuilder();
            for (Map.Entry<String, BigDecimal> entry : total.entrySet()) {
                OutPutPollInfo poll = outPoll.get(entry.getKey());
                if (null == poll) {
                    continue;
                }
                DataHourLeftEmissionInfo info = new DataHourLeftEmissionInfo();
                info.setOutPutId(out.getOutPutId());
                info.setPollutantCode(poll.getPollutantCode());
                info.setStandardValue(poll.getOverMaxValue());
                info.setAvgValue(entry.getValue().divide(size, 3, RoundingMode.HALF_UP));
                if (null != minuteDataInterval && minuteDataInterval > 0 && null != poll.getOverMaxValue()) {
                    // 小时排放剩余控制 = （每小时接收的分钟数据条数*该因子标准值-该因子当前小时排放总和）/ 当前小时剩余排放分钟数据条数
                    info.setSurplusValue(minuteMonitorTotal
                            .multiply(poll.getOverMaxValue())
                            .subtract(entry.getValue())
                            .divide(minuteSurplusCount, 3, RoundingMode.HALF_UP));
                }
                leftList.add(info);
                // 使用均值计算分钟数据是否达到小时数据超标预警，废气排口才会生成小时数据超标预警
                if (alarmEnabled && OutPutTypeEnum.OUT_PUT_FQ.code.equals(out.getOutPutType()) && info.getAvgValue().compareTo(poll.getOverMaxValue()) > 0) {
                    if (redisCacheUtils.ifSendByCache(out.getOutPutId(), poll.getOutPutCode(),
                            DataTypeEnum.minute.code.toString(), AlarmDetailTypeEnum.WARN_LARGE.code.toString())) {
                        bu.append(poll.getPollutantNameCn());
                        bu.append(" ").append(info.getAvgValue()).append("(").append(poll.getOverMaxValue()).append(")");
                        bu.append("、");
                        BaseDurAlarm warm = new BaseDurAlarm();
                        warm.setAlarmId(UlidCreator.getMonotonicUlid().toString());
                        warm.setOutPutId(out.getOutPutId());
                        warm.setPollutantCode(poll.getPollutantCode());
                        warm.setDataType(DataTypeEnum.minute.code);
                        warm.setAlarmType(AlarmDetailTypeEnum.WARN_LARGE.code);
                        warm.setAlarmTime(last.getMonitorTime());
                        warm.setAlarmStatus(Constants.ALARM_STATUS_ACTIVE);
                        warm.setAlarmMsg(info.getAvgValue() + "(" + poll.getOverMaxValue() + ")");
                        warnList.add(warm);
                    }
                }
            }
            if (bu.length() > 0) {
                /* 发送预警消息 */
                String message = bu.substring(0, bu.length() - 1);
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(out.getEntCode())
                        .entName(out.getEntName())
                        .outPutCode(out.getOutPutCode())
                        .outPutName(out.getOutPutName())
                        .outPutType(out.getOutPutType())
                        .dataType(DataTypeEnum.minute.code)
                        .alarmType(AlarmDetailTypeEnum.WARN_LARGE.code)
                        .alarmTime(date)
                        .message(message)
                        .build());
                weComSend.sendWXMessage(out.getWeComMsg(), "【数据超标预警-小时数据超标】：" + out.getEntName() + " " + out.getOutPutName() + "；" +
                        date.format(DateUtils.yy_m_d_h) + " " + bu.substring(0, bu.length() - 1) + " 请您注意！");
            }
        }
        XxlJobHelper.log("预警数据入库 {}", warnList.size());
        // 删除旧的预警信息
        baseAlarmMapper.deleteOldWarn(null, DataTypeEnum.minute.code,
                Collections.singletonList(AlarmDetailTypeEnum.WARN_LARGE.code));
        if (!warnList.isEmpty()) {
            // 插入或更新预警信息
            baseAlarmMapper.batchInsertWarn(warnList);
        }
        XxlJobHelper.log("小时剩余控制数据更新条数 {}", leftList.size());
        // 清空剩余控制表表
        taskHandlerMapper.truncateHourLeftEmission();
        if (!leftList.isEmpty()) {
            List<DataHourLeftEmissionInfo> sub = new ArrayList<>();
            for (DataHourLeftEmissionInfo info : leftList) {
                sub.add(info);
                if (sub.size() > 100) {
                    taskHandlerMapper.batchInsertHourLeftEmission(sub);
                    sub.clear();
                }
            }
            if (!sub.isEmpty()) {
                taskHandlerMapper.batchInsertHourLeftEmission(sub);
            }
        }
    }
}

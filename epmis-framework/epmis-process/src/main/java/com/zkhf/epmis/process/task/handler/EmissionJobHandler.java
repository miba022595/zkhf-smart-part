package com.zkhf.epmis.process.task.handler;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.OutPutPollInfo;
import com.zkhf.epmis.process.base.utils.ProcessTools;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.task.TaskHandlerMapper;
import com.zkhf.epmis.process.send.weixin.WeComSend;
import com.zkhf.epmis.process.sse.domain.SseOnlineAlarm;
import com.zkhf.epmis.process.sse.service.SseMessagingService;
import com.zkhf.epmis.process.task.domain.EmissionTask;
import com.zkhf.epmis.process.task.domain.PollDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 年排放量检测任务
 */
@Component
public class EmissionJobHandler {

    private TaskHandlerMapper taskHandlerMapper;
    @Autowired
    public void setTaskHandlerMapper(TaskHandlerMapper taskHandlerMapper) {
        this.taskHandlerMapper = taskHandlerMapper;
    }

    private ProcessTools processTools;
    @Autowired
    public void setProcessTools(ProcessTools processTools) {
        this.processTools = processTools;
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
     * 污染物年排量检测任务--日数据，每天查询一次
     */
    @XxlJob("pollPaiFangCouTaskHandler")
    public void pollPaiFangCouTaskHandler() {
        // 获取昨天时间（防止跨年）
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        // 获取当年时间
        LocalDateTime start = LocalDateTime.of(date.getYear(), 1, 1, 0, 0, 0),
                end = LocalDateTime.of(date.getYear(), 12, 31, 23, 59, 59);
        // 数据表名称组成：t_data_out_年份_排口id
        String tableName = "t_data_out_" + DateUtils.getTableYear(date) + "_";
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
        // 表名忽略了大小写
        outList.forEach( e -> outMap.put(e.getOutPutId().toLowerCase(), e));
        // 获取排口污染物信息
        List<OutPutPollInfo> pollInfos = redisCacheUtils.getAllOutPutPollList();
        Map<String, Map<String, OutPutPollInfo>> outPollMap = new HashMap<>();
        // 企业的月排量限值统计
        Map<String, Map<String, BigDecimal>> entMLimitMap = new HashMap<>();
        int montVal = date.getMonthValue();
        Map<String, String> pollCodeName = new HashMap<>();
        for (OutPutPollInfo e : pollInfos) {
            pollCodeName.put(e.getPollutantCode(), e.getPollutantNameCn());
            if (StringUtils.isEmpty(e.getOutPutId())) {
                continue;
            }
            // 排放量需要勾选累计排放量的才会进行计算
            if (StringUtils.isEmpty(e.getMonFactor()) || !e.getMonFactor().contains("cou")) {
                continue;
            }
            Map<String, OutPutPollInfo> map = outPollMap.computeIfAbsent(e.getOutPutId().toLowerCase(), k -> new HashMap<>());
            map.put(e.getPollutantCode(), e);
            // 获取截至到当月的排放量限值
            BigDecimal nowLimit = processTools.getToNowLimitValue(e, montVal);
            if (null == nowLimit) {
                continue;
            }
            Map<String, BigDecimal> entMonth = entMLimitMap.computeIfAbsent(e.getEntCode(), k -> new HashMap<>());
            if (entMonth.containsKey(e.getPollutantCode())) {
                entMonth.put(e.getPollutantCode(), entMonth.get(e.getPollutantCode()).add(nowLimit));
            } else {
                entMonth.put(e.getPollutantCode(), nowLimit);
            }
        }
        // 排口主键id
        String lowerOutId;
        int len = tableName.length();
        // 登记报警列表
        List<EmissionTask> annualList = new ArrayList<>();
        // 排放量数据
        Map<String, Map<String, BigDecimal>> entEmissionMap = new HashMap<>();
        Map<String, OutPutInfo> entInfoMap = new HashMap<>();
        for (String name : tableNames) {
            lowerOutId = name.substring(len);
            OutPutInfo out = outMap.get(lowerOutId);
            if (null == out) {
                XxlJobHelper.log("未找到对应的排口 {}", lowerOutId);
                continue;
            }
            entInfoMap.put(out.getEntCode(), out);
            Map<String, OutPutPollInfo> outPoll = outPollMap.get(lowerOutId);
            if (null == outPoll || outPoll.isEmpty()) {
                XxlJobHelper.log("企业 {} 排口 {} 未勾选污染物信息", out.getEntName(), out.getOutPutName());
                continue;
            }
            // 获取日数据列表
            List<PollDataInfo> dataList = taskHandlerMapper.getOutPutDataList(name, DataTypeEnum.day.code, start, end);
            if (null == dataList || dataList.size() < 2) {
                continue;
            }
            Map<String, BigDecimal> cou = new HashMap<>(); // 总量map
            for (PollDataInfo val : dataList) {
                parseData(val, cou);
            }
            for (Map.Entry<String, BigDecimal> entry : cou.entrySet()) {
                String pollCode = entry.getKey();
                // 排放量
                BigDecimal emissions = entry.getValue();
                OutPutPollInfo poll = outPoll.get(pollCode);
                if (null == poll) {
                    continue;
                }
                // 添加年排量信息
                EmissionTask annual = new EmissionTask();
                annual.setEntCode(out.getEntCode());
                annual.setOutPutId(out.getOutPutId());
                annual.setEmissionYear(date.getYear());
                annual.setPollutantCode(poll.getPollutantCode());
                annual.setEmissions(emissions);
                annualList.add(annual);
                // 按企业-污染物进行统计
                Map<String, BigDecimal> entSub = entEmissionMap.computeIfAbsent(poll.getEntCode(), k -> new HashMap<>());
                if (entSub.containsKey(pollCode)) {
                    entSub.put(pollCode, entSub.get(pollCode).add(emissions));
                } else {
                    entSub.put(pollCode, emissions);
                }
            }
        }
        XxlJobHelper.log("污染物年排量检测更新条数 {}", annualList.size());
        if (annualList.isEmpty())
            return;
        List<EmissionTask> sub = new ArrayList<>();
        for (EmissionTask info : annualList) {
            sub.add(info);
            if (sub.size() > 100) {
                taskHandlerMapper.batchInsertOrUpdateEmissions(sub);
                sub.clear();
            }
        }
        if (!sub.isEmpty()) {
            taskHandlerMapper.batchInsertOrUpdateEmissions(sub);
        }
        // 获取企业年排量限值
        Map<String, Map<String, BigDecimal>> entYLimitMap = processTools.getEntYLimitMap(date.getYear());
        XxlJobHelper.log("排放总量超标预警报警检测条数 {}", entEmissionMap.size());
        // 判断企业的排放量是否超标
        for (String entCode : entEmissionMap.keySet()) {
            OutPutInfo info = entInfoMap.get(entCode);
            if (null == info) {
                continue;
            }
            // 年排量限值
            Map<String, BigDecimal> yLimitMap = entYLimitMap.get(entCode);
            // 月排量
            Map<String, BigDecimal> mLimitMap = entMLimitMap.get(entCode);
            StringBuilder yuj = new StringBuilder(), baoj = new StringBuilder();
            boolean sendYujing = false, sendBaoJing = false;
            Map<String, BigDecimal> entSub = entEmissionMap.get(entCode);
            for (String pollCode : entSub.keySet()) {
                BigDecimal yLimit = null == yLimitMap ? null : yLimitMap.get(pollCode);
                BigDecimal mLimit = null == mLimitMap ? null : mLimitMap.get(pollCode);
                BigDecimal value = entSub.get(pollCode);
                if (null != yLimit && value.compareTo(yLimit) >= 0) {
                    sendBaoJing = true;
                    yuj.append(pollCodeName.get(pollCode)).append("-").append(value).append("(").append(yLimit).append(")、");
                    continue;
                }
                // 通过月份判断是否触发预警
                if (null != mLimit && value.compareTo(mLimit) >= 0) {
                    sendYujing = true;
                    yuj.append(pollCodeName.get(pollCode)).append("-").append(value).append("(").append(mLimit).append(")、");
                }
            }
            if (sendBaoJing) {
                // 发送微信消息
                String message = baoj.deleteCharAt(baoj.length() - 1).toString();
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(info.getEntCode())
                        .entName(info.getEntName())
                        .outPutCode(info.getOutPutCode())
                        .outPutName(info.getOutPutName())
                        .outPutType(info.getOutPutType())
                        .dataType(DataTypeEnum.hour.code)
                        .alarmType(AlarmDetailTypeEnum.ALARM_TOTAL_EMISSION.code)
                        .alarmTime(end)
                        .message(message)
                        .build());
                weComSend.sendWXMessage(info.getWeComMsg(), "【数据超标预警-排放总量超标】：" + info.getEntName() + "，"
                        + info.getOutPutName() + "；" + message + " 请您注意！");
                XxlJobHelper.log(baoj.toString());
            }
            if (sendYujing) {
                // 发送微信消息
                String message = yuj.deleteCharAt(yuj.length() - 1).toString();
                sseMessagingService.notifyBroadcastAlarmInfo(SseOnlineAlarm.builder()
                        .entCode(info.getEntCode())
                        .entName(info.getEntName())
                        .outPutCode(info.getOutPutCode())
                        .outPutName(info.getOutPutName())
                        .outPutType(info.getOutPutType())
                        .dataType(DataTypeEnum.hour.code)
                        .alarmType(AlarmDetailTypeEnum.WARN_TOTAL_EMISSION.code)
                        .alarmTime(end)
                        .message(message)
                        .build());
                weComSend.sendWXMessage(info.getWeComMsg(), "【数据超标报警-排放总量超标】：" + info.getEntName() + "，"
                        + info.getOutPutName() + "；" + message + " 请您注意！");
                XxlJobHelper.log(yuj.toString());
            }
        }
    }

    private void parseData(PollDataInfo data, Map<String, BigDecimal> cou) {
        if (null == data || StringUtils.isEmpty(data.getDataInfo())) {
            return;
        }
        try {
            // {"a00000": {"avg": "6.34", "cou": "3806.33", "max": "6.99", "min": "5.12"}
            JSONObject info = JSONObject.parseObject(data.getDataInfo());
            BigDecimal decimal;
            for (String pollCode : info.keySet()) {
                Map<String, Object> obj = info.getJSONObject(pollCode);
                if (null == obj || obj.isEmpty()) {
                    continue;
                }
                String val = MapUtil.getStr(obj, "cou");
                if (StringUtils.isEmpty(val)) {
                    continue;
                }
                decimal = new BigDecimal(val);
                if (cou.containsKey(pollCode)) {
                    cou.put(pollCode, decimal.add(cou.get(pollCode)));
                } else {
                    cou.put(pollCode, decimal);
                }
            }
        } catch (Exception ignore) {
        }
    }

}

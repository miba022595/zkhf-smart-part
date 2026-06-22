package com.zkhf.epmis.process.ai.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.HeadInfo;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.ai.domain.AiDataReq;
import com.zkhf.epmis.process.ai.domain.AiOutPutData;
import com.zkhf.epmis.process.ai.domain.DataInfo;
import com.zkhf.epmis.process.ai.service.AiDataService;
import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.PollutantCode;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.ai.AiDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class AiDataServiceImpl implements AiDataService {

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    private AiDataMapper aiDataMapper;
    @Autowired
    public void setOutPutOnlineMapper(AiDataMapper aiDataMapper) {
        this.aiDataMapper = aiDataMapper;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    @Override
    public AjaxResult onlineMonitorList(AiDataReq req) {
        List<AiOutPutData> list = new ArrayList<>();
        log.info("排口在线监测列表查询: {}", req);
        AjaxResult result = AjaxResult.success(list);
        Map<String, OutPutInfo> infoMap = new HashMap<>();
        // 获取数据
        List<DataInfo> dataList = selectDataList(req, infoMap);
        if (dataList.isEmpty()) {
            return result;
        }
        List<String> outPutIds = new ArrayList<>();
        infoMap.values().forEach( e -> outPutIds.add(e.getOutPutId()));
        Map<String, List<PollHead>> heads = platformFacade.multipleAutoHeads(outPutIds, req.getDataEnum());

        Map<String, AiOutPutData> outPutDataMap = new HashMap<>();
        for (DataInfo data : dataList) {
            OutPutInfo out = infoMap.get(data.getTableName());
            if (out == null) {
                continue;
            }
            if (StringUtils.isEmpty(data.getDataInfoStr())) {
                continue;
            }
            data.setDataMap(JSONObject.parseObject(data.getDataInfoStr()));
            AiOutPutData aiData = outPutDataMap.get(out.getOutPutId());
            if (aiData == null) {
                aiData = new AiOutPutData();
                aiData.setOutInfo(out);
                aiData.setDataList(new ArrayList<>());
                aiData.setHeadList(heads.get(out.getOutPutId()));
                outPutDataMap.put(out.getOutPutId(), aiData);
                list.add(aiData);
            }
            aiData.getDataList().add(data);
        }
        if (list.isEmpty()) {
            return result;
        }
        list.forEach( e -> e.getDataList().sort((o1, o2) -> o2.getMonitorTime().compareTo(o1.getMonitorTime())));
        return result;
    }

    @Override
    public String onlineMonitorListText(AiDataReq req) {
        log.info("排口在线监测文本查询: {}", req);
        Map<String, OutPutInfo> infoMap = new HashMap<>();
        List<DataInfo> dataList = selectDataList(req, infoMap);
        if (dataList.isEmpty()) {
            return "";
        }
        List<String> outPutIds = new ArrayList<>();
        infoMap.values().forEach(e -> outPutIds.add(e.getOutPutId()));
        Map<String, List<PollHead>> heads = platformFacade.multipleAutoHeads(outPutIds, req.getDataEnum());

        Map<String, OutPutInfo> outMap = new HashMap<>();
        Map<String, List<DataInfo>> outDataMap = new HashMap<>();
        for (DataInfo data : dataList) {
            OutPutInfo out = infoMap.get(data.getTableName());
            if (out == null) {
                continue;
            }
            if (StringUtils.isEmpty(data.getDataInfoStr())) {
                continue;
            }
            data.setDataMap(JSONObject.parseObject(data.getDataInfoStr()));
            outMap.put(out.getOutPutId(), out);
            outDataMap.computeIfAbsent(out.getOutPutId(), k -> new ArrayList<>()).add(data);
        }
        if (outDataMap.isEmpty()) {
            return "";
        }

        List<String> sortedOutPutIds = new ArrayList<>(outDataMap.keySet());
        sortedOutPutIds.sort(String::compareTo);
        StringBuilder text = new StringBuilder();
        for (String outPutId : sortedOutPutIds) {
            OutPutInfo out = outMap.get(outPutId);
            if (out == null) {
                continue;
            }
            List<DataInfo> outList = outDataMap.get(outPutId);
            if (outList == null || outList.isEmpty()) {
                continue;
            }
            String section = formatOnlineMonitorText(out, heads == null ? null : heads.get(outPutId), outList, req);
            if (StringUtils.isEmpty(section)) {
                continue;
            }
            if (!text.isEmpty()) {
                text.append("\n\n");
            }
            text.append(section);
        }
        return text.toString();
    }

    private String formatOnlineMonitorText(OutPutInfo out, List<PollHead> headList, List<DataInfo> dataList, AiDataReq req) {
        if (out == null || dataList == null || dataList.isEmpty()) {
            return "";
        }
        dataList.sort(Comparator.comparing(DataInfo::getMonitorTime));
        LocalDateTime start = dataList.getFirst().getMonitorTime();
        LocalDateTime end = dataList.getLast().getMonitorTime();
        if (start == null || end == null) {
            return "";
        }

        String entName = StringUtils.isNotEmpty(out.getEntName()) ? out.getEntName() : "";
        String outPutName = StringUtils.isNotEmpty(out.getOutPutName()) ? out.getOutPutName() : "";
        String outTypeName = OutPutTypeEnum.getNameByCode(out.getOutPutType());

        List<PollHead> polls = headList == null ? new ArrayList<>() : new ArrayList<>(headList);
        if (polls.isEmpty()) {
            return "";
        }

        StringBuilder bu = new StringBuilder();
        bu.append("企业：").append(entName).append("；排口：").append(outPutName).append("；排口类型：").append(outTypeName).append("\n");
        bu.append("起始时间: ").append(start.format(DateUtils.yy_m_d_h_m_s)).append("\n");
        bu.append("结束时间: ").append(end.format(DateUtils.yy_m_d_h_m_s)).append("\n");
        bu.append("顺序: 时间正序(从起始到结束)\n");
        bu.append("实际记录: ").append(dataList.size()).append(" 条\n\n");

        String intervalUnit = "秒";
        long divisor = 1;
        if (req != null) {
            if (DataEnum.minute.name().equals(req.getDataEnum())) {
                intervalUnit = "分";
                divisor = 60;
            } else if (DataEnum.hour.name().equals(req.getDataEnum())) {
                intervalUnit = "时";
                divisor = 3600;
            } else if (DataEnum.day.name().equals(req.getDataEnum())) {
                intervalUnit = "天";
                divisor = 86400;
            }
        }

        String dataEnum = req == null ? null : req.getDataEnum();
        boolean isRealData = DataEnum.real.name().equals(dataEnum);
        List<String> columnHeaders = new ArrayList<>();
        List<String> columnPollCodes = new ArrayList<>();
        List<String> columnFactorNames = new ArrayList<>();
        for (PollHead p : polls) {
            if (p == null || StringUtils.isEmpty(p.getPollCode())) {
                continue;
            }
            String pollutantName = StringUtils.isNotEmpty(p.getPollName()) ? p.getPollName() : p.getPollCode();
            List<HeadInfo> headInfos = p.getHeadList();
            if (headInfos == null || headInfos.isEmpty()) {
                continue;
            }
            for (HeadInfo h : headInfos) {
                if (h == null || StringUtils.isEmpty(h.getName())) {
                    continue;
                }
                if (isRealData) {
                    if (!"rtd".equals(h.getName()) && !"zsRtd".equals(h.getName())) {
                        continue;
                    }
                } else {
                    if ("rtd".equals(h.getName()) || "zsRtd".equals(h.getName())) {
                        continue;
                    }
                }
                String unit = "cou".equals(h.getName()) ? p.getCouUnit() : p.getRtdUnit();
                StringBuilder header = new StringBuilder();
                header.append(pollutantName);
                if (StringUtils.isNotEmpty(h.getDesc())) {
                    header.append("-").append(h.getDesc());
                } else {
                    header.append("-").append(h.getName());
                }
                if (StringUtils.isNotEmpty(unit)) {
                    header.append("(").append(unit).append(")");
                }
                columnPollCodes.add(p.getPollCode());
                columnFactorNames.add(h.getName());
                columnHeaders.add(header.toString());
            }
        }
        if (columnHeaders.isEmpty()) {
            return "";
        }

        bu.append("数据:\n");
        bu.append("间隔(").append(intervalUnit).append(")");
        for (String header : columnHeaders) {
            bu.append(" | ").append(header);
        }
        bu.append("\n");

        LocalDateTime lastTime = null;
        for (DataInfo d : dataList) {
            if (d == null || d.getMonitorTime() == null) {
                continue;
            }
            if (lastTime == null) {
                bu.append("--");
            } else {
                long diffSeconds = Math.max(0, Duration.between(lastTime, d.getMonitorTime()).getSeconds());
                bu.append(formatInterval(diffSeconds, divisor));
            }
            for (int i = 0; i < columnHeaders.size(); i++) {
                bu.append(" | ").append(formatPollValue(d, columnPollCodes.get(i), columnFactorNames.get(i)));
            }
            bu.append("\n");
            lastTime = d.getMonitorTime();
        }
        return bu.toString().trim();
    }

    private String formatInterval(long diffSeconds, long divisor) {
        if (divisor <= 1) {
            return String.valueOf(diffSeconds);
        }
        BigDecimal val = BigDecimal.valueOf(diffSeconds)
                .divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return val.toPlainString();
    }

    private String formatPollValue(DataInfo data, String pollCode, String factorName) {
        if (data == null || data.getDataMap() == null || StringUtils.isEmpty(pollCode) || StringUtils.isEmpty(factorName)) {
            return "--";
        }
        Object pollObj = data.getDataMap().get(pollCode);
        if (!(pollObj instanceof Map)) {
            return "--";
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> pollMap = (Map<String, Object>) pollObj;
        Object val = pollMap.get(factorName);
        String str = val == null ? "--" : String.valueOf(val);
        if (StringUtils.isEmpty(str) || "null".equalsIgnoreCase(str)) {
            str = "--";
        }
        if (!"--".equals(str) && ("rtd".equals(factorName) || "avg".equals(factorName) || "zsRtd".equals(factorName) || "zsAvg".equals(factorName))) {
            Object flag = pollMap.get("flag");
            if (flag != null) {
                String flagStr = String.valueOf(flag);
                if (StringUtils.isNotEmpty(flagStr) && !"N".equalsIgnoreCase(flagStr)) {
                    str = str + "(" + flagStr + ")";
                }
            }
        }
        return str;
    }

    private List<DataInfo> selectDataList(AiDataReq req, Map<String, OutPutInfo> infoMap) {
        List<DataInfo> dataList = new ArrayList<>();
        if (null == req || StringUtils.isEmpty(req.getBeginTime()) || StringUtils.isEmpty(req.getEndTime())) {
            return dataList;
        }
        LocalDateTime time;
        if (DataEnum.real.name().equals(req.getDataEnum())) {
            req.setDataType(DataTypeEnum.real.code);
        } else if (DataEnum.minute.name().equals(req.getDataEnum())) {
            req.setDataType(DataTypeEnum.minute.code);
        } else if (DataEnum.hour.name().equals(req.getDataEnum())) {
            req.setDataType(DataTypeEnum.hour.code);
        } else if (DataEnum.day.name().equals(req.getDataEnum())) {
            req.setDataType(DataTypeEnum.day.code);
        } else {
            return dataList;
        }
        time = LocalDateTime.parse(req.getBeginTime(), DateUtils.yy_m_d_h_m_s);
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        // 获取要查询的表名
        // 添加权限
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty()) {
                return dataList;
            }
        }
        for (OutPutInfo info : outPutList) {
            if (StringUtils.isEmpty(info.getEntCode())) {
                continue;
            }
            if (null != entCodes && !entCodes.contains(info.getEntCode())) {
                continue;
            }
            if (null != req.getOutPutType() && !req.getOutPutType().equals(info.getOutPutType())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getEntName()) &&
                    (StringUtils.isEmpty(info.getEntName()) || !info.getEntName().contains(req.getEntName()))) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getOutPutName()) &&
                    (StringUtils.isEmpty(info.getOutPutName()) || !info.getOutPutName().contains(req.getOutPutName()))) {
                continue;
            }
            String tableName = "t_data_out_" + DateUtils.getTableYear(time) + "_" + info.getOutPutId().toLowerCase();
            infoMap.put(tableName, info);
        }
        if (infoMap.isEmpty()) {
            return dataList;
        }
        // 判断是否存在表
        List<String> tableNames = aiDataMapper.selectTableNameList(new ArrayList<>(infoMap.keySet()));
        // 限制查询的表个数
        if (tableNames.isEmpty() || tableNames.size() > 10) {
            return dataList;
        }
        return aiDataMapper.selectDataList(tableNames, req);
    }

    @Override
    public AjaxResult selectAlarmList(AiDataReq req) {
        if (null == req) {
            req = new AiDataReq();
        }
        // 企业排口信息筛选
        Map<String, OutPutInfo> outMap = new HashMap<>();
        reqDeal(req, outMap);
        if (outMap.isEmpty()) {
            return AjaxResult.success(new ArrayList<>());
        }
        List<DurAlarmInfo> list = aiDataMapper.selectAlarmList(req, outMap);
        // 填充数据
        fillAlarmDetail(list, outMap);
        return AjaxResult.success(list);
    }

    private void reqDeal(AiDataReq req, Map<String, OutPutInfo> outMap) {
        // 添加权限
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty()) {
                return;
            }
        }
        // 获取权限下的企业和排口信息
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        if (null == outPutList || outPutList.isEmpty()) {
            return;
        }
        for (OutPutInfo info : outPutList) {
            if (StringUtils.isEmpty(info.getEntCode())) {
                continue;
            }
            if (null != entCodes && !entCodes.contains(info.getEntCode())) {
                continue;
            }
            if (null != req.getOutPutType() && !req.getOutPutType().equals(info.getOutPutType())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getEntName()) && (StringUtils.isEmpty(info.getEntName()) || !info.getEntName().contains(req.getEntName()))) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getOutPutName()) && (StringUtils.isEmpty(info.getOutPutName()) || !info.getOutPutName().contains(req.getOutPutName()))) {
                continue;
            }
            outMap.put(info.getOutPutId(), info);
        }
    }

    private void fillAlarmDetail(List<DurAlarmInfo> list, Map<String, OutPutInfo> outMap) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<PollutantCode> allPoll = redisCacheUtils.getAllPollDataList();
        Map<String, PollutantCode> allPollMap = new HashMap<>();
        if (null != allPoll && !allPoll.isEmpty()) {
            for (PollutantCode poll : allPoll) {
                allPollMap.put(poll.getPollutantCode(), poll);
            }
        }
        // 判断报警
        for (DurAlarmInfo info : list) {
            if (null != info.getStartTime() && null != info.getEndTime()) {
                info.setDuration(DateUtils.convertMinutes(Duration.between(info.getStartTime(), info.getEndTime()).toMinutes()));
            }
            info.setAlarmTypeDesc(AlarmDetailTypeEnum.getNameByCode(info.getAlarmType()).name);
            OutPutInfo outPutInfo = outMap.get(info.getOutPutId());
            if (null == outPutInfo) {
                continue;
            }
            info.setOutPutId(outPutInfo.getOutPutId());
            info.setOutPutCode(outPutInfo.getOutPutCode());
            info.setOutPutName(outPutInfo.getOutPutName());
            info.setOutPutType(outPutInfo.getOutPutType());
            info.setOutPutTypeDesc(OutPutTypeEnum.getNameByCode(outPutInfo.getOutPutType()));
            info.setEntCode(outPutInfo.getEntCode());
            info.setEntName(outPutInfo.getEntName());
            // 构建报警信息：2025-09-10 04:00:00_鹊山矿井水排放口_超标报警_氨氮_小时数据_监测值:1.022(0-1)
            StringBuilder bu = new StringBuilder();
            if (null != info.getStartTime()) {
                bu.append(info.getStartTime().format(DateUtils.yy_m_d_h_m_s)).append("_");
            }
            bu.append(outPutInfo.getEntName()).append(outPutInfo.getOutPutName()).append("_");
            PollutantCode poll;
            if (AlarmDetailTypeEnum.ALARM_HOUR_IMPERFECT.code.equals(info.getAlarmType())) {
                info.setPollutantCode(info.getAlarmMsg());
                StringBuilder pollutantNameCn = new StringBuilder(), pollutantNameEn = new StringBuilder();
                for (String code : info.getPollutantCode().split(",")) {
                    if (!allPollMap.containsKey(code)) {
                        continue;
                    }
                    poll = allPollMap.get(code);
                    if (!pollutantNameCn.isEmpty()) {
                        pollutantNameCn.append(",");
                        pollutantNameEn.append(",");
                    }
                    pollutantNameCn.append(poll.getPollutantNameCn());
                    pollutantNameEn.append(poll.getPollutantNameEn());
                }
                info.setPollutantNameCn(pollutantNameCn.toString());
                info.setPollutantNameEn(pollutantNameEn.toString());
            } else {
                poll = allPollMap.get(info.getPollutantCode());
                if (null != poll) {
                    info.setPollutantNameCn(poll.getPollutantNameCn());
                    info.setPollutantNameEn(poll.getPollutantNameEn());
                }
            }
            if (AlarmDetailTypeEnum.ALARM_LARGE.code.equals(info.getAlarmType())) {
                if (DataTypeEnum.minute.code.equals(info.getDataType())) {
                    bu.append("超标报警_").append(info.getPollutantNameCn()).append("_")
                            .append("分钟数据_监测值:").append(info.getAlarmMsg());
                } else {
                    bu.append("超标报警_").append(info.getPollutantNameCn()).append("_")
                            .append("小时数据_监测值:").append(info.getAlarmMsg());
                }
            } else if (AlarmDetailTypeEnum.ALARM_SMALL.code.equals(info.getAlarmType())) {
                bu.append("超下限报警_").append(info.getPollutantNameCn()).append("_")
                        .append("小时数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.ALARM_ZERO.code.equals(info.getAlarmType())) {
                bu.append("零值报警_").append(info.getPollutantNameCn()).append("_")
                        .append("小时数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.ALARM_CONSTANT.code.equals(info.getAlarmType())) {
                bu.append("恒值报警_").append(info.getPollutantNameCn()).append("_")
                        .append("小时数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.ALARM_NEGATIVE.code.equals(info.getAlarmType())) {
                bu.append("负值报警_").append(info.getPollutantNameCn()).append("_")
                        .append("小时数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.ALARM_NET_ERR.code.equals(info.getAlarmType())) {
                bu.append("联网异常报警");
            } else if (AlarmDetailTypeEnum.ALARM_HOUR_MISS.code.equals(info.getAlarmType())) {
                bu.append("小时数据缺失报警");
            } else if (AlarmDetailTypeEnum.ALARM_HOUR_IMPERFECT.code.equals(info.getAlarmType())) {
                bu.append("小时数据不完整报警_").append(info.getPollutantNameCn());
            } else if (AlarmDetailTypeEnum.WARN_LARGE.code.equals(info.getAlarmType())) {
                bu.append("超标预警_").append(info.getPollutantNameCn()).append("_")
                        .append("分钟数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.WARN_SMALL.code.equals(info.getAlarmType())) {
                bu.append("超下限预警_").append(info.getPollutantNameCn()).append("_")
                        .append("分钟数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.WARN_ZERO.code.equals(info.getAlarmType())) {
                bu.append("零值预警_").append(info.getPollutantNameCn()).append("_")
                        .append("分钟数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.WARN_CONSTANT.code.equals(info.getAlarmType())) {
                bu.append("恒值预警_").append(info.getPollutantNameCn()).append("_")
                        .append("分钟数据_监测值:").append(info.getAlarmMsg());
            } else if (AlarmDetailTypeEnum.WARN_NEGATIVE.code.equals(info.getAlarmType())) {
                bu.append("负值预警_").append(info.getPollutantNameCn()).append("_")
                        .append("分钟数据_监测值:").append(info.getAlarmMsg());
            }
            info.setAlarmMsg(bu.toString());
        }
    }
}

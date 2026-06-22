package com.zkhf.epmis.process.homePage.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.base.domain.*;
import com.zkhf.epmis.process.base.utils.ProcessTools;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.homePage.domain.HomePageEmissions;
import com.zkhf.epmis.process.homePage.domain.HomePageEmissionsPoll;
import com.zkhf.epmis.process.homePage.domain.HomePageSignInfo;
import com.zkhf.epmis.process.homePage.domain.HomePageSignInfoDetail;
import com.zkhf.epmis.process.homePage.service.HomePageService;
import com.zkhf.epmis.process.mapper.homePage.HomePageMapper;
import com.zkhf.epmis.process.onlineMonitoring.domain.OutPutOnlineData;
import com.zkhf.epmis.process.statistics.domain.EntEmission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页 Service业务层处理
 */
@Slf4j
@Service
public class HomePageServiceImpl implements HomePageService {

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    private HomePageMapper homePageMapper;
    @Autowired
    public void setHomePageMapper(HomePageMapper homePageMapper) {
        this.homePageMapper = homePageMapper;
    }

    private ProcessTools processTools;
    @Autowired
    public void setProcessTools(ProcessTools processTools) {
        this.processTools = processTools;
    }

    @Override
    public AjaxResult homePageEmissionsList() {
        List<HomePageEmissions> homePageList = new ArrayList<>();
        AjaxResult result =  AjaxResult.success(homePageList);
        // 获取关注的排口列表
        List<UserAttentionInfo> userAttentionList = platformFacade.selectUserAttentionList(GVarContainer.getUserId());
        // 获取所有排口污染物信息
        List<OutPutPollInfo> outPutPollInfoList = redisCacheUtils.getAllOutPutPollList();
        // 过滤只包含排放量选项的污染物
        outPutPollInfoList = filterPoll(userAttentionList, outPutPollInfoList);
        if (outPutPollInfoList.isEmpty()) {
            return result;
        }
        // 分类，并构建响应结构
        List<String> outPutIdList = new ArrayList<>();
        // 对list进行聚合
        Map<String, String> lowOutPutIdMap = new HashMap<>();
        Map<String, HomePageEmissions> resultMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        String tableName = "t_data_out_" + DateUtils.getTableYear(now) + "_";
        outPutPollInfoList.forEach( e -> {
            if (!outPutIdList.contains(e.getOutPutId())) {
                outPutIdList.add(e.getOutPutId());
            }
            String lowOutPutId = e.getOutPutId().toLowerCase();
            lowOutPutIdMap.put(tableName + lowOutPutId, lowOutPutId);
            HomePageEmissions info = resultMap.get(lowOutPutId);
            if (null == info) {
                info = HomePageEmissions.builder()
                        .outPutId(e.getOutPutId())
                        .entCode(e.getEntCode())
                        .entName(e.getEntName())
                        .outPutCode(e.getOutPutCode())
                        .outPutName(e.getOutPutName())
                        .outPutType(e.getOutPutType())
                        .mnNUm(e.getMnNum())
                        .pollList(new ArrayList<>())
                        .build();
                homePageList.add(info);
                resultMap.put(lowOutPutId, info);
            }
            info.getPollList().add(HomePageEmissionsPoll.builder()
                    .pollutantCode(e.getPollutantCode())
                    .pollutantNameEn(e.getPollutantNameEn())
                    .pollutantNameCn(e.getPollutantNameCn())
                    .dataList(new ArrayList<>())
                    .build());
        });
        // 获取小时累计数据
        List<OutEmissionsInfo> infoList = homePageMapper.selectOutEmissionsList(outPutIdList);
        Map<String, Map<String, OutEmissionsInfo>> infoDataMap = new HashMap<>();
        infoList.forEach( e -> {
            String lowOutPutId = e.getOutPutId().toLowerCase();
            if (!infoDataMap.containsKey(lowOutPutId)) {
                infoDataMap.put(lowOutPutId, new HashMap<>());
            }
            infoDataMap.get(lowOutPutId).put(e.getPollutantCode(), e);
        });
        resultMap.forEach((k, v) -> {
            Map<String, OutEmissionsInfo> sub = infoDataMap.get(k);
            if (null != sub) {
                v.getPollList().forEach( e -> {
                    OutEmissionsInfo info = sub.get(e.getPollutantCode());
                    if (null != info) {
                        e.setStandardValue(info.getStandardValue());
                        e.setAvgValue(info.getAvgValue());
                        e.setSurplusValue(info.getSurplusValue());
                    }
                });
            }
        });
        // 按排口主键id倒序排列
        homePageList.sort((o1, o2) -> o2.getOutPutId().compareTo(o1.getOutPutId()));
        homePageList.forEach( e -> e.getPollList().sort(Comparator.comparing(HomePageEmissionsPoll::getPollutantCode)));
        // 获取分钟数据
        String nowStr = now.format(DateUtils.yy_m_d);
        List<String> tableNames = homePageMapper.selectTableName(tableName + "%");
        List<String> selTables = new ArrayList<>();
        for (String name : tableNames) {
            if (lowOutPutIdMap.containsKey(name)) {
                selTables.add(name);
            }
        }
        if (selTables.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> list = homePageMapper.selectOutMinuteList(selTables, DataTypeEnum.minute.code, 10,
                nowStr + " 00:00:00", nowStr + " 23:59:59");
        // 解析数据
        for (Map<String, Object> e : list) {
            HomePageEmissions info = resultMap.get(lowOutPutIdMap.get(MapUtil.getStr(e, "tableName")));
            if (null == info) {
                continue;
            }
            String dataInfo = MapUtil.getStr(e, "dataInfo");
            if (StringUtils.isEmpty(dataInfo)) {
                continue;
            }
            String monitorTime = MapUtil.getStr(e, "monitorTime");
            JSONObject data = JSONObject.parseObject(dataInfo);
            Map<String, BigDecimal> pollData = new HashMap<>();
            for (String pollCode : data.keySet()) {
                JSONObject poll = JSONObject.parseObject(data.getString(pollCode));
                // 取污染物的均值、折算均值数据
                pollData.put(pollCode, poll.getBigDecimal("avg"));
                pollData.put(pollCode + "_zs", poll.getBigDecimal("zsAvg"));
            }
            info.getPollList().forEach( f -> {
                Map<String, Object> item = new HashMap<>();
                item.put("monitorTime", monitorTime);
                item.put("avg", pollData.get(f.getPollutantCode()));
                item.put("zsAvg", pollData.get(f.getPollutantCode() + "_zs"));
                f.getDataList().add(item);
            });
        }
        return result;
    }

    @Override
    public AjaxResult homePageRealDataList(String outPutId) {
        AjaxResult result = AjaxResult.success();
        List<Map<String, Object>> headList = new ArrayList<>();
        List<OutPutOnlineData> dataList = new ArrayList<>();
        result.put("head", headList);
        result.put("data", dataList);
        // 获取所有排口污染物信息
        List<OutPutPollInfo> outPutPollInfoList = redisCacheUtils.getAllOutPutPollList();
        if (null == outPutPollInfoList || outPutPollInfoList.isEmpty()) {
            return result;
        }
        // 筛选过滤，分类
        for (OutPutPollInfo info : outPutPollInfoList) {
            if (StringUtils.isEmpty(info.getOutPutId()) || !info.getOutPutId().equals(outPutId)) {
                continue;
            }
            if (StringUtils.isEmpty(info.getMonFactor())) {
                continue;
            }
            Map<String, Object> head = new HashMap<>();
            headList.add(head);
            head.put("pollutantCode", info.getPollutantCode());
            head.put("pollutantNameCn", info.getPollutantNameCn());
            head.put("pollutantNameEn", info.getPollutantNameEn());
            head.put("pollutantUnitCn", info.getPollutantUnitCn());
            head.put("pollutantUnitEn", info.getPollutantUnitEn());
            List<Map<String, Object>> subHeadList = new ArrayList<>();
            head.put("headList", subHeadList);
            for (String factor : info.getMonFactor().split(",")) {
                if ("rtd".equals(factor) || "zsRtd".equals(factor)) {
                    Map<String, Object> subHead = new HashMap<>();
                    subHead.put("name", factor);
                    subHead.put("desc", "rtd".equals(factor) ? "实测值" : "折算值");
                    subHeadList.add(subHead);
                }
            }
        }
        // 获取实时数据
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DateUtils.yy_m_d);
        String tableName = "t_data_out_" + DateUtils.getTableYear(now) + "_" + outPutId.toLowerCase();
        List<String> tableNames = homePageMapper.selectTableName(tableName);
        List<OutPutOnlineData> list = null;
        if (null != tableNames && tableNames.contains(tableName)) {
            list = homePageMapper.selectOutRealList(tableName, DataTypeEnum.real.code, 60,
                    nowStr + " 00:00:00", nowStr + " 23:59:59");
        }
        if (null != list) {
            list.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getDataInfoStr())) {
                    e.setDataMap(JSONObject.parseObject(e.getDataInfoStr()));
                }
            });
        } else {
            list = new ArrayList<>();
        }
        result.put("data", list);
        return result;
    }

    /**
     * 过滤只包含排放量选项的污染物
     */
    private List<OutPutPollInfo> filterPoll(List<UserAttentionInfo> userAttentionList, List<OutPutPollInfo> outPutPollInfoList) {
        List<OutPutPollInfo> result = new ArrayList<>();
        if (null == userAttentionList || userAttentionList.isEmpty() || null == outPutPollInfoList || outPutPollInfoList.isEmpty()) {
            return result;
        }
        // 核心分组代码：按 outPutId 分组
        Map<String, List<OutPutPollInfo>> groupedByOutputId = new HashMap<>();
        for (OutPutPollInfo info : outPutPollInfoList) {
            if (StringUtils.isEmpty(info.getOutPutId())) {
                continue;
            }
            if (!groupedByOutputId.containsKey(info.getOutPutId())) {
                groupedByOutputId.put(info.getOutPutId(), new ArrayList<>());
            }
            groupedByOutputId.get(info.getOutPutId()).add(info);
        }
        // 筛选判断
        for (UserAttentionInfo att : userAttentionList) {
            List<OutPutPollInfo> subList = groupedByOutputId.get(att.getOutPutId());
            if (null == subList) {
                continue;
            }
            for (OutPutPollInfo sub : subList) {
                if (StringUtils.isNotEmpty(sub.getMonFactor()) && sub.getMonFactor().contains("cou")) {
                    result.add(sub);
                }
            }
        }
        return result;
    }

    @Override
    public AjaxResult cockpit24hPlantTrend(String entCode, Integer outPutType, String outPutId, String pollutantCode) {
        AjaxResult result = AjaxResult.success();
        // 小时数据最新时间为上一个小时数据
        LocalDateTime entDate = LocalDateTime.now().minusHours(1);
        // 按小时进行填充
        List<String> dateList = get24hHourList(entDate);
        Map<String, Map<String, Object>> infoMap = new HashMap<>();
        Map<String, Map<String, Object>> pollMap = new HashMap<>();
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        result.put("outInfo", infoMap.values());
        result.put("pollInfo", pollMap.values());
        result.put("data", dataMap.values());
        result.put("hours", dateList);
        if (StringUtils.isEmpty(entCode)) {
            return result;
        }
        // 获取所有排口污染物信息
        List<OutPutPollInfo> outPutPollInfoList = redisCacheUtils.getAllOutPutPollList();
        if (outPutPollInfoList.isEmpty()) {
            return result;
        }
        List<PollutantCode> pollInfoList = redisCacheUtils.getAllPollDataList();
        Map<String, PollutantCode> pollInfoMap = new HashMap<>();
        pollInfoList.forEach( e -> pollInfoMap.put(e.getPollutantCode(), e));
        // 对list进行聚合
        Map<String, String> lowOutPutIdMap = new HashMap<>();
        String tableName = "t_data_out_" + DateUtils.getTableYear(entDate) + "_";
        for (OutPutPollInfo outPoll : outPutPollInfoList) {
            if (!entCode.equals(outPoll.getEntCode())) {
                continue;
            }
            if (null != outPutType && !outPutType.equals(outPoll.getOutPutType())) {
                continue;
            }
            if (StringUtils.isNotEmpty(outPutId) && !outPutId.equals(outPoll.getOutPutId())) {
                continue;
            }
            if (StringUtils.isNotEmpty(pollutantCode) && !pollutantCode.equals(outPoll.getPollutantCode())) {
                continue;
            }
            String lowOutPutId = outPoll.getOutPutId().toLowerCase();
            lowOutPutIdMap.put(tableName + lowOutPutId, outPoll.getOutPutId());
            if (!infoMap.containsKey(outPoll.getOutPutId())) {
                Map<String, Object> info = new HashMap<>();
                info.put("entCode", outPoll.getEntCode());
                info.put("entName", outPoll.getEntName());
                info.put("outPutId", outPoll.getOutPutId());
                info.put("outPutCode", outPoll.getOutPutCode());
                info.put("outPutName", outPoll.getOutPutName());
                info.put("outPutType", outPoll.getOutPutType());
                info.put("outPutTypeDesc", OutPutTypeEnum.getNameByCode(outPoll.getOutPutType()));
                infoMap.put(outPoll.getOutPutId(), info);
            }
            if (!pollMap.containsKey(outPoll.getPollutantCode()) && pollInfoMap.containsKey(outPoll.getPollutantCode())) {
                PollutantCode code = pollInfoMap.get(outPoll.getPollutantCode());
                Map<String, Object> info = new HashMap<>();
                info.put("pollutantCode", outPoll.getPollutantCode());
                info.put("pollutantNameCn", code.getPollutantNameCn());
                info.put("pollutantUnitEn", code.getPollutantUnitEn());
                pollMap.put(outPoll.getPollutantCode(), info);
            }
        }
        // 获取24小时内时间
        String nowStr = entDate.format(DateUtils.yy_m_d_h);
        String startStr = entDate.minusHours(23).format(DateUtils.yy_m_d_h);
        List<String> tableNames = homePageMapper.selectTableName(tableName + "%");
        // 过滤
        List<String> selTables = tableNames.stream().filter(lowOutPutIdMap::containsKey).collect(Collectors.toList());
        if (selTables.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> list = homePageMapper.selectOutDataList(selTables, "asc", DataTypeEnum.hour.code, null,
                startStr + ":00:00", nowStr + ":59:59");
        // 解析数据
        Map<String, Map<String, Map<String, Object>>> outPollDataMap = new HashMap<>();
        // 构建响应体
        for (Map<String, Object> e : list) {
            String outId = lowOutPutIdMap.get(MapUtil.getStr(e, "tableName"));
            if (null == outId) {
                continue;
            }
            String monitorTime = MapUtil.getStr(e, "monitorTime");
            if (StringUtils.isEmpty(monitorTime)) {
                continue;
            }
            // 去除年, yyyy-MM-dd...
            monitorTime = monitorTime.substring(5);
            String dataInfo = MapUtil.getStr(e, "dataInfo");
            if (StringUtils.isEmpty(dataInfo)) {
                continue;
            }
            if (!dataMap.containsKey(outId)) {
                dataMap.put(outId, new HashMap<>());
                dataMap.get(outId).put("outId", outId);
            }
            Map<String, Object> pollDataMap = outPollDataMap.computeIfAbsent(outId, k -> new HashMap<>()).computeIfAbsent(monitorTime, k -> new HashMap<>());
            JSONObject data = JSONObject.parseObject(dataInfo);
            for (String pollCode : data.keySet()) {
                if (StringUtils.isNotEmpty(pollutantCode) && !pollutantCode.equals(pollCode)) {
                    continue;
                }
                dataMap.get(outId).put(pollCode, new ArrayList<>());
                JSONObject poll = JSONObject.parseObject(data.getString(pollCode));
                // 取污染物的均值、折算均值数据
                pollDataMap.put(pollCode, getDataMap(poll.getBigDecimal("avg"), poll.getBigDecimal("zsAvg")));
            }
        }
        dataMap.forEach( (k, v) -> {
            // k -> outId   sk -> pollCode
            v.forEach( (sk, sv) -> {
                if (!"outId".equals(sk) && sv instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> dL = (List<Object>)sv;
                    dateList.forEach( e -> {
                        Map<String, Object> pollDataMap = outPollDataMap.get(k).get(e);
                        if (null != pollDataMap && pollDataMap.containsKey(sk)) {
                            dL.add(pollDataMap.get(sk));
                        } else {
                            dL.add(getDataMap(null, null));
                        }
                    });
                }
            });
        });
        return result;
    }

    private List<String> get24hHourList(LocalDateTime now) {
        List<String> dateList = new ArrayList<>();
        LocalDateTime start = now.minusHours(23);
        while (!start.isAfter(now)) {
            dateList.add(start.format(DateUtils.m_d_h));
            start = start.plusHours(1);
        }
        return dateList;
    }

    private Map<String, BigDecimal> getDataMap(BigDecimal avg, BigDecimal zsAvg) {
        Map<String, BigDecimal> dataMap = new HashMap<>();
        dataMap.put("avg", avg);
        dataMap.put("zsAvg", zsAvg);
        return dataMap;
    }

    @Override
    public AjaxResult cockpitAlarmStatistics(String entCode, Integer queryType) {
        AjaxResult result = AjaxResult.success();
        // 小时数据最新时间为上一个小时数据
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> dataList = new ArrayList<>();
        result.put("data", dataList);
        if (StringUtils.isEmpty(entCode)) {
            return result;
        }
        String start;
        String end = now.format(DateUtils.yy_m_d_h_m_s);
        if (DataTypeEnum.year.code.equals(queryType)) {
            start = String.format("%d-01-01 00:00:00", now.getYear());
        } else if (DataTypeEnum.month.code.equals(queryType)) {
            start = String.format("%d-%02d-01 00:00:00", now.getYear(), now.getMonthValue());
        } else if (DataTypeEnum.week.code.equals(queryType)) {
            LocalDateTime weekStart = now.with(DayOfWeek.MONDAY);
            start = String.format("%d-%02d-%02d 00:00:00",
                    weekStart.getYear(), weekStart.getMonthValue(), weekStart.getDayOfMonth());
        } else {
            start = String.format("%d-%02d-%02d 00:00:00", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        }
        // 获取所有排口信息
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        if (outPutList.isEmpty()) {
            return result;
        }
        // 对list进行聚合
        List<String> outPutIds = new ArrayList<>();
        for (OutPutInfo out : outPutList) {
            if (!entCode.equals(out.getEntCode())) {
                continue;
            }
            outPutIds.add(out.getOutPutId());
        }
        if (outPutIds.isEmpty()) {
            return result;
        }
        List<Map<String, Object>> list = homePageMapper.selectOutAlarmCount(outPutIds, start, end);
        list.forEach( e -> {
            AlarmDetailTypeEnum type = AlarmDetailTypeEnum.getNameByCode(MapUtil.getInt(e, "alarmType"));
            if (type.code > 0) { // 只要报警的信息
                Map<String, Object> data = new HashMap<>();
                data.put("alarmType", type.name);
                data.put("alarmCount", MapUtil.getInt(e, "alarmCount"));
                dataList.add(data);
            }
        });
        return result;
    }

    @Override
    public AjaxResult cockpitEmissions(String entCode, Integer emissionYear) {
        List<JSONObject> list = new ArrayList<>();
        AjaxResult result = AjaxResult.success(list);
        if (GVarContainer.isNotAdmin()) {
            List<String> entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty() || !entCodes.contains(entCode)) {
                return result;
            }
        }
        if (null == emissionYear || emissionYear < 2020) {
            emissionYear = LocalDate.now().getYear();
        }
        // 查询企业资质的排放量信息-年排放量限值
        Map<String, Map<String, BigDecimal>> entYLimitMap = processTools.getEntYLimitMap(emissionYear);
        // 企业的年排量限值
        Map<String, BigDecimal> yLimitMap = entYLimitMap.get(entCode);
        if (null == yLimitMap || yLimitMap.isEmpty()) {
            return result;
        }
        List<EntEmission> emList = homePageMapper.cockpitEmissions(entCode, emissionYear);
        Map<String, BigDecimal> emMap = new HashMap<>();
        if (null != emList && !emList.isEmpty()) {
            emList.forEach( e -> emMap.put(e.getPollutantCode(), e.getEmissions()));
        }
        // 获取污染物信息
        List<PollutantCode> codeList = redisCacheUtils.getAllPollDataList();
        Map<String, PollutantCode> codeMap = new HashMap<>();
        codeList.forEach( e -> codeMap.put(e.getPollutantCode(), e));
        yLimitMap.forEach( (k, v) -> {
            PollutantCode pollCode = codeMap.get(k);
            if (null != pollCode) {
                JSONObject data = new JSONObject();
                data.put("pollutantCode", k);
                data.put("pollutantNameCn", pollCode.getPollutantNameCn());
                data.put("pollutantNameEn", pollCode.getPollutantNameEn());
                data.put("couUnit", pollCode.getUnitPfEn());
                data.put("yLimit", v);
                data.put("emissions", emMap.get(k));
                list.add(data);
            }

        });
        return result;
    }

    @Override
    public AjaxResult signInfo(Integer queryType) {
        // 企业权限
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
        }
        HomePageSignInfo info = new HomePageSignInfo();
        // 传输有效率设置默认值
        info.setEffectiveTrans(getHomePageSignInfoDetail());
        // 标记完成率设置默认值
        info.setCompletion(getHomePageSignInfoDetail());
        LocalDateTime now = LocalDateTime.now();
        String start;
        String end = now.format(DateUtils.yy_m_d_h_m_s);
        if (DataTypeEnum.year.code.equals(queryType)) {
            start = String.format("%d-01-01 00:00:00", now.getYear());
        } else if (DataTypeEnum.month.code.equals(queryType)) {
            start = String.format("%d-%02d-01 00:00:00", now.getYear(), now.getMonthValue());
        } else if (DataTypeEnum.week.code.equals(queryType)) {
            LocalDateTime weekStart = now.with(DayOfWeek.MONDAY);
            start = String.format("%d-%02d-%02d 00:00:00",
                    weekStart.getYear(), weekStart.getMonthValue(), weekStart.getDayOfMonth());
        } else { // 默认当日的
            start = String.format("%d-%02d-%02d 00:00:00", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        }
        // 获取所有排口
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        // 查询存在的数据表
        String tableName = "t_data_out_";
        String tableDataName = "t_data_out_" + DateUtils.getTableYear(now) + "_";
        Map<String, String> outTableMap = new HashMap<>();
        List<String> outPutIds = new ArrayList<>();
        for (OutPutInfo out : outPutList) {
            if (null != entCodes && !entCodes.contains(out.getEntCode())) {
                continue;
            }
            outTableMap.put(tableDataName + out.getOutPutId().toLowerCase(), out.getOutPutId());
            outPutIds.add(out.getOutPutId());
        }
        List<String> tableNames = homePageMapper.selectTableName(tableName + "%");
        List<String> selData = new ArrayList<>();
        for (String item : tableNames) {
            if (outTableMap.containsKey(item)) {
                selData.add(item);
            }
        }
        // 获取总的小时条数，（时间用于判断是否报警）
        List<Map<String, String>> outDateList = homePageMapper.selectOutDataTimeList(selData, DataTypeEnum.hour.code, start, end);
        // 获取报警列表
        int overLimit = 0;
        if (null != outDateList && !outDateList.isEmpty() && !outPutIds.isEmpty()) {
            List<DurAlarmInfo> alarmList = homePageMapper.selectAlarmList(outPutIds, DataTypeEnum.hour.code, start, end);
            // 计算超标（超上限）的报警条数
            for (DurAlarmInfo alarm : alarmList) {
                if (AlarmDetailTypeEnum.ALARM_LARGE.code.equals(alarm.getAlarmType())) {
                    overLimit++;
                }
            }
            // 报警数量
            Integer alarmSize = null;
            for (Map<String, String> data : outDateList) {
                LocalDateTime time = DateUtils.strToLocalDateTime(MapUtil.getStr(data, "monitorTime"), DateUtils.yy_m_d_h);
                if (null == time) {
                    continue;
                }
                String outPutId = outTableMap.get(MapUtil.getStr(data, "tableName"));
                for (DurAlarmInfo alarm : alarmList) {
                    if (!outPutId.equals(alarm.getOutPutId())) {
                        continue;
                    }
                    // 判断数据的时间点是否命中报警时间范围
                    if (time.isBefore(alarm.getStartTime())) {
                        continue;
                    }
                    if (null != alarm.getEndTime() && time.isAfter(alarm.getEndTime())) {
                        continue;
                    }
                    if (null == alarmSize) {
                        alarmSize = 0;
                    }
                    alarmSize++;
                    break;
                }
            }
            // 通过小时数据计算达标率（总的-不报警的=达标的）
            HomePageSignInfoDetail detail = new HomePageSignInfoDetail();
            detail.setTotal(outDateList.size());
            detail.setComplete(detail.getTotal() - (null == alarmSize ? 0 : alarmSize));
            detail.setRate((detail.getComplete() * 10000) / (detail.getTotal()) / 100.0f);
            info.setAchievement(detail);
        }
        info.setOverLimit(overLimit);
        return AjaxResult.success(info);
    }

    private HomePageSignInfoDetail getHomePageSignInfoDetail() {
        // 设置默认值
        HomePageSignInfoDetail detail = new HomePageSignInfoDetail();
        detail.setTotal(0);
        detail.setComplete(0);
        detail.setRate(0.0f);
        return detail;
    }
}

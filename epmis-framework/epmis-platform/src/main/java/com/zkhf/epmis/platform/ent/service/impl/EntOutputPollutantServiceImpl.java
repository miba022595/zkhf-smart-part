package com.zkhf.epmis.platform.ent.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.HeadInfo;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.DataEnum;
import com.zkhf.epmis.core.enums.DataFactorEnum;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.ent.domain.EntAutoHead;
import com.zkhf.epmis.platform.ent.domain.EntOutputPollutant;
import com.zkhf.epmis.platform.ent.service.EntOutputPollutantService;
import com.zkhf.epmis.platform.mapper.ent.EntOutputPollutantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业排口污染物信息Service业务层处理
 */
@Service
public class EntOutputPollutantServiceImpl implements EntOutputPollutantService {

    private EntOutputPollutantMapper entOutputPollutantMapper;

    @Autowired
    public void setEntOutputPollutantMapper(EntOutputPollutantMapper entOutputPollutantMapper) {
        this.entOutputPollutantMapper = entOutputPollutantMapper;
    }

    @Override
    public List<Map<String, Object>> selectPollutantCodesByOutPutId(String outPutId) {
        return entOutputPollutantMapper.selectPollutantCodesByOutPutId(outPutId);
    }

    @Override
    public List<Map<String, Object>> listAll() {
        return entOutputPollutantMapper.listAll();
    }

    @Override
    public List<EntOutputPollutant> selectOutputPollutantByOutPutId(String outPutId) {
        return entOutputPollutantMapper.selectOutputPollutantByOutPutId(outPutId);
    }

    @Override
    public List<EntAutoHead> selectAutoHead(String outPutId, String dataEnum) {
        List<EntAutoHead> headList = entOutputPollutantMapper.selectAutoHead(outPutId);
        for (EntAutoHead e : headList) {
            if (StringUtils.isNotEmpty(e.getRtdUnit())) {
                e.setPollutantUnitEn(e.getRtdUnit());
                e.setPollutantUnitCn(e.getRtdUnit());
            }
            if (StringUtils.isNotEmpty(e.getCouUnit())) {
                e.setUnitPfCn(e.getCouUnit());
                e.setUnitPfEn(e.getCouUnit());
            }
            if (StringUtils.isEmpty(e.getMonFactor())) {
                continue;
            }
            List<HeadInfo> subList = new ArrayList<>();
            e.setHeadList(subList);
            String[] monFactors = e.getMonFactor().split(",");
            for (String name : monFactors) {
                String desc = DataFactorEnum.getNameByCode(name);
                if (StringUtils.isEmpty(desc)) {
                    continue;
                }
                addSubHead(dataEnum, subList, name, desc);
            }
        }
        return headList;
    }

    private void addSubHead(String dataEnum, List<HeadInfo> subList, String name, String desc) {
        if (DataEnum.real.name().equals(dataEnum)) {
            // 实时数据查看实测值值、折算实测值
            if (DataFactorEnum.rtd.code.equals(name)
                    || DataFactorEnum.zsRtd.code.equals(name)) {
                subList.add(HeadInfo.builder().name(name).desc(desc).build());
            }
        } else {
            // 非实时数据不查看实测值
            if (!DataFactorEnum.rtd.code.equals(name)
                    && !DataFactorEnum.zsRtd.code.equals(name)) {
                subList.add(HeadInfo.builder().name(name).desc(desc).build());
            }
        }
    }

    @Override
    public List<Map<String, Object>> autoHeadChart(String outPutId) {
        List<Map<String, Object>> list = entOutputPollutantMapper.autoHeadChart(outPutId);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                String rtdUnit = MapUtil.getStr(map, "rtdUnit");
                if (StringUtils.isNotEmpty(rtdUnit)) {
                    map.put("pollutantUnitEn", rtdUnit);
                }
            }
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> multipleAutoHead(List<String> outPutIds, String dataEnum) {
        List<Map<String, Object>> headList = entOutputPollutantMapper.multipleAutoHead(outPutIds);
        // 先拼接组合，后整理，保证顺序一致
        Map<String, Map<String, Object>> headMap = new HashMap<>();
        Map<String, List<String>> factorMap = new HashMap<>();
        for (Map<String, Object> e : headList) {
            String rtdUnit = MapUtil.getStr(e, "rtdUnit");
            if (StringUtils.isNotEmpty(rtdUnit)) {
                e.put("pollutantUnitEn", rtdUnit);
            }
            String couUnit = MapUtil.getStr(e, "couUnit");
            if (StringUtils.isNotEmpty(couUnit)) {
                e.put("unitPfEn", couUnit);
            }
            String pollutantCode = MapUtil.getStr(e, "pollutantCode");
            if (StringUtils.isEmpty(pollutantCode)) {
                continue;
            }
            String monFactor = MapUtil.getStr(e, "monFactor");
            if (StringUtils.isEmpty(monFactor)) {
                continue;
            }
            List<HeadInfo> subList;
            if (headMap.containsKey(pollutantCode)) {
                subList = getSubList(headMap, pollutantCode);
            } else {
                subList = new ArrayList<>();
                e.put("headList", subList);
                e.remove("monFactor");
                headMap.put(pollutantCode, e);
            }
            List<String> factorList = factorMap.computeIfAbsent(pollutantCode, k -> new ArrayList<>());
            String[] monFactors = monFactor.split(",");
            for (String name : monFactors) {
                String desc = DataFactorEnum.getNameByCode(name);
                if (StringUtils.isEmpty(desc)) {
                    continue;
                }
                if (factorList.contains(name)) {
                    continue;
                }
                factorList.add(name);
                addSubHead(dataEnum, subList, name, desc);
            }
        }
        headList = new ArrayList<>(headMap.values());
        headList.sort((o1, o2) -> MapUtil.getInt(o2, "pollutantSort", 0) - MapUtil.getInt(o1, "pollutantSort", 0));
        return headList;
    }

    @Override
    public Map<String, List<PollHead>> multipleAutoHeads(List<String> outPutIds, String dataEnum) {
        Map<String, List<PollHead>> factorMap = new HashMap<>();
        if (null == outPutIds || outPutIds.isEmpty()) {
            return factorMap;
        }
        List<Map<String, Object>> headList = entOutputPollutantMapper.multipleAutoHead(outPutIds);
        for (Map<String, Object> e : headList) {
            String pollutantCode = MapUtil.getStr(e, "pollutantCode");
            if (StringUtils.isEmpty(pollutantCode)) {
                continue;
            }
            String monFactor = MapUtil.getStr(e, "monFactor");
            if (StringUtils.isEmpty(monFactor)) {
                continue;
            }
            String rtdUnit = MapUtil.getStr(e, "rtdUnit");
            if (StringUtils.isEmpty(rtdUnit)) {
                rtdUnit = MapUtil.getStr(e, "pollutantUnitEn");
            }
            String couUnit = MapUtil.getStr(e, "couUnit");
            if (StringUtils.isEmpty(couUnit)) {
                couUnit = MapUtil.getStr(e, "unitPfEn");
            }
            List<HeadInfo> subList = new ArrayList<>();
            String[] monFactors = monFactor.split(",");
            for (String name : monFactors) {
                String desc = DataFactorEnum.getNameByCode(name);
                if (StringUtils.isEmpty(desc)) {
                    continue;
                }
                addSubHead(dataEnum, subList, name, desc);
            }
            List<PollHead> item;
            String outPutId = MapUtil.getStr(e, "outPutId");
            if (factorMap.containsKey(outPutId)) {
                item = factorMap.get(outPutId);
            } else {
                item = new ArrayList<>();
                factorMap.put(outPutId, item);
            }
            PollHead head = new PollHead();
            head.setPollCode(pollutantCode);
            head.setPollName(MapUtil.getStr(e, "pollutantNameCn"));
            head.setRtdUnit(rtdUnit);
            head.setCouUnit(couUnit);
            head.setDecimalPlaces(MapUtil.getInt(e, "decimalPlaces"));
            head.setHeadList(subList);
            item.add(head);
        }
        return factorMap;
    }

    @SuppressWarnings("unchecked")
    private List<HeadInfo> getSubList(Map<String, Map<String, Object>> headMap, String pollutantCode) {
         return (List<HeadInfo>)headMap.get(pollutantCode).get("headList");
    }

    @Override
    public AjaxResult selectOutputPollutantById(String outPutId) {
        return AjaxResult.success(entOutputPollutantMapper.selectOutputPollutantById(outPutId));
    }

    @Override
    @Log(title = "新增企业排口污染物信息", businessType = BusinessType.INSERT)
    public AjaxResult insertOutputPollutant(EntOutputPollutant poll) {
        if (poll.getOutPutId() == null) {
            return AjaxResult.error("未指定排口");
        }
        poll.setOutPutPollId(UlidCreator.getMonotonicUlid().toString());
        poll.setCreateTime(LocalDateTime.now());
        int result = entOutputPollutantMapper.insertOutputPollutant(poll);
        String warn = null;
        if (result > 0) {
            // 修改排口信息中的污染物列表
            entOutputPollutantMapper.updateOutPutPollCodeById(poll.getOutPutId());
            // 排污许可的排放量分配提示信息
            warn = emissionAllocationReminder(poll.getOutPutId(), poll.getPollutantCode(), poll.getMonFactor());
        }
        AjaxResult ajaxResult = AjaxResult.success(poll);
        ajaxResult.put("warn", warn);
        return ajaxResult;
    }

    @Override
    @Log(title = "修改企业排口污染物信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateOutputPollutant(EntOutputPollutant poll) {
        poll.setUpdateTime(LocalDateTime.now());
        int result = entOutputPollutantMapper.updateOutputPollutant(poll);
        String warn = null;
        if (result > 0) {
            // 修改排口信息中的污染物列表
            if (null == poll.getOutPutId()) { // 请求参数没传outPutId时
                poll = entOutputPollutantMapper.selectOutputPollutantById(poll.getOutPutPollId());
            }
            if (null != poll && null != poll.getOutPutId()) {
                entOutputPollutantMapper.updateOutPutPollCodeById(poll.getOutPutId());
                // 排污许可的排放量分配提示信息
                warn = emissionAllocationReminder(poll.getOutPutId(), poll.getPollutantCode(), poll.getMonFactor());
            }
        }
        AjaxResult ajaxResult = AjaxResult.success(poll);
        ajaxResult.put("warn", warn);
        return ajaxResult;
    }

    @Override
    @Log(title = "删除企业排口污染物信息", businessType = BusinessType.DELETE)
    public AjaxResult deleteOutputPollutantById(String outPutPollId) {
        EntOutputPollutant poll = entOutputPollutantMapper.selectOutputPollutantById(outPutPollId);
        if (null != poll) {
            int result = entOutputPollutantMapper.deleteOutputPollutantById(outPutPollId);
            if (result > 0) {
                // 修改排口信息中的污染物列表
                entOutputPollutantMapper.updateOutPutPollCodeById(poll.getOutPutId());
            }
        }
        return AjaxResult.success();
    }

    /**
     * 排污许可的排放量分配提示
     */
    private String emissionAllocationReminder(String outPutId, String pollCode, String monFactor) {
        // 污染物数据项勾选排放量时进行年排量检测
        if (StringUtils.isEmpty(monFactor) || !monFactor.contains("cou")) {
            return null;
        }
        LocalDate now = LocalDate.now();
        // 查询排污许可上配置的年排放量数据
        BigDecimal yPermit = entOutputPollutantMapper.selectEntOutPollutantPermitCount(outPutId, pollCode, now.getYear());
        if (null == yPermit) {
            return "排污许可未配置污染物的年排放量数据，请注意！";
        }
        // 查询排口所在企业的所有月排放量数据
        List<String> mLimitList = entOutputPollutantMapper.selectEntOutPollutantMonthlyLimit(outPutId, pollCode);
        if (null == mLimitList || mLimitList.isEmpty()) {
            return "排口未配置污染物的月排放量信息，请注意！";
        }
        BigDecimal all = BigDecimal.ZERO, item;
        for (String mLimit : mLimitList) {
            for (String limit : mLimit.split(",")) {
                if (StringUtils.isEmpty(limit)) {
                    continue;
                }
                try {
                    item = new BigDecimal(limit);
                    all = all.add(item);
                } catch (Exception ignore) {}
            }
        }
        if (all.compareTo(yPermit) > 0) {
            return "排污许可配置的年排放量(" + toDouble(yPermit) + ")，排口的累计月排放量(" + toDouble(all) + ")，分配超出限额，请注意！";
        } else if (all.compareTo(yPermit) < 0) {
            return "排污许可配置的年排放量(" + toDouble(yPermit) + ")，排口的累计月排放量(" + toDouble(all) + ")，未完全分配，请注意！";
        }
        return null;
    }

    private double toDouble(BigDecimal decimal) {
        if (null == decimal) {
            return 0;
        }
        return decimal.setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}

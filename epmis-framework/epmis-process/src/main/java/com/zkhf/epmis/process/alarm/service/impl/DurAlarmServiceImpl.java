package com.zkhf.epmis.process.alarm.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.*;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.process.alarm.domain.*;
import com.zkhf.epmis.process.alarm.service.DurAlarmService;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.PollutantCode;
import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.auth.AuthFacade;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.alarm.DurAlarmMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DurAlarmServiceImpl implements DurAlarmService {

    private DurAlarmMapper alarmMapper;
    @Autowired
    public void setDurAlarmMapper(DurAlarmMapper alarmMapper) {
        this.alarmMapper = alarmMapper;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    private AuthFacade authFacade;
    @Autowired
    public void setAuthFacade(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult outPutStatusList() {
        // 获取权限下所有的排口id列表
        List<String> entCodes;
        List<?> emptyList = new ArrayList<>();
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty()) {
                return AjaxResult.success(emptyList);
            }
        } else {
            entCodes = new ArrayList<>();
        }
        List<Map<String, Object>> list = platformFacade.outPutStatusList(entCodes);
        if (null == list || list.isEmpty()) {
            return AjaxResult.success(emptyList);
        }
        // 获取在线的设备列表
        List<String> onlineList = redisCacheUtils.getAllOutPutOnlineList();
        // 排口状态正常时，获取最新的报警信息
        Map<String, OutPutStatus> statusMap = new HashMap<>();
        list.forEach( e -> {
            String outPutId = MapUtil.getStr(e, "outPutId");
            // 在线时设置判断数据状态
            Integer outPutStatus = MapUtil.getInt(e, "outPutStatus");
            if (null == outPutStatus) { // 默认离线
                outPutStatus = OutPutStatusEnum.OUT_PUT_STATUS_LX.code;
            }
            // 在线离线状态变更
            if (null != onlineList) {
                if (onlineList.contains(outPutId)) {
                    if (OutPutStatusEnum.OUT_PUT_STATUS_LX.code.equals(outPutStatus)) {
                        outPutStatus = OutPutStatusEnum.OUT_PUT_STATUS_ZC.code;
                    }
                } else {
                    if (OutPutStatusEnum.OUT_PUT_STATUS_ZC.code.equals(outPutStatus)) {
                        outPutStatus = OutPutStatusEnum.OUT_PUT_STATUS_LX.code;
                    }
                }
            }
            OutPutStatus status = OutPutStatus.builder()
                    .outPutId(outPutId)
                    .outPutStatus(outPutStatus)
                    .build();
            statusMap.put(outPutId, status);
        });
        // 报警展示优先级：超标 > 预警 > 异常
        // 取未解除的报警+所有的预警，然后依据报警级别进行报警设置
        List<DurAlarmInfo> alarmList = alarmMapper.selectActiveAlarmList();
        for (DurAlarmInfo e : alarmList) {
            OutPutStatus old = statusMap.get(e.getOutPutId());
            if (null == old) {
                continue;
            }
            // 报警编码转为报警等级
            int level = AlarmDetailTypeEnum.getNameByCode(e.getAlarmType()).level;
            Integer oldLevel = old.getAlarmLevel();
            if (null == oldLevel || oldLevel > level) {
                old.setAlarmLevel(level);
            }
        }
        return AjaxResult.success(statusMap.values());
    }

    @Override
    public AjaxResult countAlarm(DurAlarmReq req) {
        if (null == req) {
            req = new DurAlarmReq();
        }
        // 企业排口信息筛选
        String reqDeal = reqDeal(req);
        if (null != reqDeal) {
            return AjaxResult.error(reqDeal);
        }
        List<DurAlarmCount> countList = alarmMapper.countAlarm(req);
        DurAlarmCount count = new DurAlarmCount();
        count.setAlarmTypeCountMap(new HashMap<>());
        if (null != countList) {
            for (DurAlarmCount c : countList) {
                if (null != c.getTotalCount()) {
                    if (null == count.getTotalCount()) {
                        count.setTotalCount(c.getTotalCount());
                    } else {
                        count.setTotalCount(c.getTotalCount() + count.getTotalCount());
                    }
                    if (count.getAlarmTypeCountMap().containsKey(c.getAlarmType())) {
                        count.getAlarmTypeCountMap().put(c.getAlarmType(), c.getTotalCount() + count.getAlarmTypeCountMap().get(c.getAlarmType()));
                    } else {
                        count.getAlarmTypeCountMap().put(c.getAlarmType(), c.getTotalCount());
                    }
                }
                if (null != c.getActiveCount()) {
                    if (null == count.getActiveCount()) {
                        count.setActiveCount(c.getActiveCount());
                    } else {
                        count.setActiveCount(c.getActiveCount() + count.getActiveCount());
                    }
                }
                if (null != c.getResolvedCount()) {
                    if (null == count.getResolvedCount()) {
                        count.setResolvedCount(c.getResolvedCount());
                    } else {
                        count.setResolvedCount(c.getResolvedCount() + count.getResolvedCount());
                    }
                }
                if (null != c.getPendingCount()) {
                    if (null == count.getPendingCount()) {
                        count.setPendingCount(c.getPendingCount());
                    } else {
                        count.setPendingCount(c.getPendingCount() + count.getPendingCount());
                    }
                }
                if (null != c.getCompletedCount()) {
                    if (null == count.getCompletedCount()) {
                        count.setCompletedCount(c.getCompletedCount());
                    } else {
                        count.setCompletedCount(c.getCompletedCount() + count.getCompletedCount());
                    }
                }
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectAlarmList(DurAlarmReq req) {
        if (null == req) {
            req = new DurAlarmReq();
        }
        // 企业排口信息筛选
        String reqDeal = reqDeal(req);
        if (null != reqDeal) {
            return AjaxResult.error(reqDeal);
        }
        PageUtils.startPage();
        List<DurAlarmInfo> list = alarmMapper.selectAlarmList(req);
        // 填充数据
        fillAlarmDetail(list, req.getOutMap());
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void exportAlarm(DurAlarmReq req, HttpServletResponse response) {
        if (null == req) {
            req = new DurAlarmReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("报警列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 企业排口信息筛选
            String reqDeal = reqDeal(req);
            List<DurAlarmInfo> list;
            if (null != reqDeal) {
                list = new ArrayList<>();
            } else {
                list = alarmMapper.selectAlarmList(req);
            }
            if (null != list && !list.isEmpty()) {
                // 填充数据
                fillAlarmDetail(list, req.getOutMap());
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (DurAlarmInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, info.getEntName(), style);
                    // 排口名称
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutName(), style);
                    // 排口类型
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutTypeDesc(), style);
                    // 报警类型
                    CellUtils.setStringVal(row, cellIndex++, info.getAlarmTypeDesc(), style);
                    // 开始时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex++, info.getStartTime(), DateUtils.yy_m_d_h_m_s, style);
                    // 结束时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex++, info.getEndTime(), DateUtils.yy_m_d_h_m_s, style);
                    // 持续时间
                    CellUtils.setStringVal(row, cellIndex++, info.getDuration(), style);
                    // 报警详情
                    CellUtils.setStringVal(row, cellIndex++, info.getAlarmMsg(), style);
                    // 报警状态，0未解除；1已解除
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (Constants.ALARM_STATUS_ACTIVE.equals(info.getAlarmStatus())) {
                        cell.setCellValue("未解除");
                    } else if (Constants.ALARM_STATUS_RESOLVED.equals(info.getAlarmStatus())) {
                        cell.setCellValue("已解除");
                    }
                    // 处理状态，0未处理；1已处理
                    cell = CellUtils.getCell(row, cellIndex, style);
                    if (Constants.DEAL_STATUS_PENDING.equals(info.getAlarmStatus())) {
                        cell.setCellValue("未处理");
                    } else if (Constants.DEAL_STATUS_COMPLETED.equals(info.getAlarmStatus())) {
                        cell.setCellValue("已处理");
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("报警列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
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
                    if (pollutantNameCn.length() > 0) {
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

    private String reqDeal(DurAlarmReq req) {
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return "无企业";
            }
        }
        // 获取权限下的企业和排口信息
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        if (null == outPutList || outPutList.isEmpty()) {
            return "无排口";
        }
        // 时间转换，起止实际同时存在
        if (StringUtils.isNotEmpty(req.getBeginTime()) && StringUtils.isNotEmpty(req.getEndTime())) {
            req.setBeginTime(req.getBeginTime() + " 00:00:00");
            req.setEndTime(req.getEndTime() + " 23:59:59");
        } else {
            req.setBeginTime(null);
            req.setEndTime(null);
        }
        Map<String, OutPutInfo> outMap = new HashMap<>();
        for (OutPutInfo info : outPutList) {
            if (StringUtils.isEmpty(info.getEntCode())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getEntCode()) && !req.getEntCode().equals(info.getEntCode())) {
                continue;
            }
            if (null != req.getEntCodes() && !req.getEntCodes().contains(info.getEntCode())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getRegion())) {// 地区匹配
                if (StringUtils.isEmpty(info.getRegion())) {
                    continue;
                }
                if (!info.getRegion().equals(req.getRegion()) && !info.getRegion().startsWith(req.getRegion() + ",")) {
                    continue;
                }
            }
            if (null != req.getOutPutType() && !req.getOutPutType().equals(info.getOutPutType())) {
                continue;
            }
            if (null != req.getOutPutIdList() && !req.getOutPutIdList().isEmpty() && !req.getOutPutIdList().contains(info.getOutPutId())) {
                continue;
            }
            outMap.put(info.getOutPutId(), info);
        }
        if (outMap.isEmpty()) {
            return "无排口";
        }
        req.setOutMap(outMap);
        return null;
    }

    @Override
    public AjaxResult selectAlarmDealList(String alarmId) {
        List<DurAlarmDeal> list = alarmMapper.selectAlarmDealList(alarmId);
        Map<String, DurAlarmDeal> annexMap = new HashMap<>();
        if (null != list && !list.isEmpty()) {
            List<SysUser> allUser = authFacade.allUserInfo();
            Map<Long, String> userMap = new HashMap<>();
            if (null != allUser) {
                allUser.forEach( u -> {
                    if (null != u.getUserId()) {
                        userMap.put(u.getUserId(), u.getUserName());
                    }
                });
            }
            list.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getDealId())) {
                    annexMap.put(e.getDealId(), e);
                }
                e.setDealUserName(userMap.get(e.getDealUserId()));
            });
        }
        if (!annexMap.isEmpty()) {
            AnnexReq req = new AnnexReq();
            req.setSourceType(AnnexTypeEnum.unifiedType.name);
            req.setSourceIds(new ArrayList<>(annexMap.keySet()));
            List<AnnexInfo> annexInfoList = platformFacade.annexList(req);
            if (null != annexInfoList && !annexInfoList.isEmpty()) {
                for (AnnexInfo annexInfo : annexInfoList) {
                    DurAlarmDeal deal = annexMap.get(annexInfo.getSourceId());
                    if (null == deal) {
                        continue;
                    }
                    if (null == deal.getAnnexList()) {
                        deal.setAnnexList(new ArrayList<>());
                    }
                    deal.getAnnexList().add(annexInfo);
                }
            }
        }
        return AjaxResult.success(list);
    }

    @Override
    @Log(title = "报警处理情况登记", businessType = BusinessType.INSERT)
    public AjaxResult insertAlarmDeal(String alarmId, DurAlarmDeal deal) {
        deal.setDealId(UlidCreator.getMonotonicUlid().toString());
        deal.setDealUserId(GVarContainer.getUserId());
        deal.setDealTime(LocalDateTime.now());
        int count = alarmMapper.insertAlarmDeal(alarmId, deal);
        if (count > 0) {
            alarmMapper.updateAlarmDealStatus(alarmId, Constants.DEAL_STATUS_COMPLETED);
            if (null != deal.getAnnexIds() && !deal.getAnnexIds().isEmpty()) {
                updateAnnex(deal.getDealId(), deal.getAnnexIds());
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "报警处理情况登记", businessType = BusinessType.DELETE)
    public AjaxResult deleteAlarmDealById(String dealId) {
        int count = alarmMapper.deleteAlarmDealById(dealId);
        if (count > 0) {
            updateAnnex(dealId, null);
        }
        return AjaxResult.success(count);
    }

    private void updateAnnex(String dealId, List<String> annexIds) {
        JSONObject req = new JSONObject();
        req.set("sourceId", dealId);
        req.set("sourceType", AnnexTypeEnum.unifiedType.name);
        req.set("annexIds", annexIds);
        platformFacade.updateAnnex(req);
    }
}


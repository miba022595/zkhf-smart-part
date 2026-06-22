package com.zkhf.epmis.process.alarm.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.alarm.domain.DurAlarmDeal;
import com.zkhf.epmis.process.alarm.domain.DurAlarmReq;
import com.zkhf.epmis.process.alarm.service.DurAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 排口在线监测 Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/durAlarm")
public class DurAlarmController {

    private DurAlarmService alarmService;
    @Autowired
    public void setDurAlarmService(DurAlarmService alarmService) {
        this.alarmService = alarmService;
    }

    /**
     * 权限下排口的报警状态
     */
    @GetMapping("/outPutStatusList")
    public AjaxResult outPutStatusList() {
        return alarmService.outPutStatusList();
    }

    /**
     * 报警统计
     */
    @PostMapping("/count")
    public AjaxResult count(@RequestBody(required = false) DurAlarmReq req) {
        return alarmService.countAlarm(req);
    }

    /**
     * 报警列表查询
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) DurAlarmReq req) {
        return alarmService.selectAlarmList(req);
    }

    /**
     * 导出报警列表
     * 按模板导出
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) DurAlarmReq req, HttpServletResponse response) {
        alarmService.exportAlarm(req, response);
    }

    /**
     * 获取报警处理
     */
    @GetMapping("/deal")
    public AjaxResult getDealList(@RequestParam String alarmId) {
        return alarmService.selectAlarmDealList(alarmId);
    }

    /**
     * 新增报警处理
     */
    @PostMapping("/deal")
    public AjaxResult addDeal(@RequestParam String alarmId, @RequestBody DurAlarmDeal deal) {
        return alarmService.insertAlarmDeal(alarmId, deal);
    }

    /**
     * 删除报警处理
     */
    @DeleteMapping("/deal")
    public AjaxResult removeDeal(@RequestParam String dealId) {
        return alarmService.deleteAlarmDealById(dealId);
    }
}


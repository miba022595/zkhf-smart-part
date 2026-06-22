package com.zkhf.epmis.process.alarm.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.alarm.domain.DurAlarmDeal;
import com.zkhf.epmis.process.alarm.domain.DurAlarmReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 排口在线监测数据-图表Service接口
 */
public interface DurAlarmService {

    /**
     * 权限下排口的报警状态
     */
    AjaxResult outPutStatusList();

    /**
     * 报警统计
     */
    AjaxResult countAlarm(DurAlarmReq req);

    /**
     * 报警列表查询
     */
    AjaxResult selectAlarmList(DurAlarmReq req);

    /**
     * 导出报警列表
     */
    void exportAlarm(DurAlarmReq req, HttpServletResponse response);

    /**
     * 查询报警处理情况登记
     */
    AjaxResult selectAlarmDealList(String alarmId);

    /**
     * 新增报警处理情况登记
     */
    AjaxResult insertAlarmDeal(String alarmId, DurAlarmDeal deal);

    /**
     * 删除报警处理情况登记信息
     */
    AjaxResult deleteAlarmDealById(String dealId);
}

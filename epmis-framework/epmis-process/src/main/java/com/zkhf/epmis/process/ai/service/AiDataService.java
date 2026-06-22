package com.zkhf.epmis.process.ai.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.ai.domain.AiDataReq;

/**
 * 排口在线监测数据-图表Service接口
 */
public interface AiDataService {

    /**
     * 获取数据列表
     */
    AjaxResult onlineMonitorList(AiDataReq req);

    /**
     * 获取数据列表-返回精简的文本格式
     */
    String onlineMonitorListText(AiDataReq req);

    /**
     * 获取报警列表
     */
    AjaxResult selectAlarmList(AiDataReq req);
}

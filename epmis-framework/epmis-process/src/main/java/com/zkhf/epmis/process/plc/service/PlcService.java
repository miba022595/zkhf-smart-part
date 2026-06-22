package com.zkhf.epmis.process.plc.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.plc.domain.PlcReq;

/**
 * 排口在线监测数据-图表Service接口
 */
public interface PlcService {

    /**
     * 获取实时工况数据（排口为空则获取全部企业的）
     */
    AjaxResult getRealTimeData(PlcReq req);

    /**
     * 获取历史工况数据
     */
    AjaxResult getHistoryData(PlcReq req);

}

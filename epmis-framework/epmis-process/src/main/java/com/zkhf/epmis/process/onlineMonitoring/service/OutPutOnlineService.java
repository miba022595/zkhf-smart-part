package com.zkhf.epmis.process.onlineMonitoring.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.onlineMonitoring.domain.OutPutOnlineReq;
import com.zkhf.epmis.process.onlineMonitoring.domain.RealDataReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 排口在线监测数据-图表Service接口
 */
public interface OutPutOnlineService {

    /**
     * 实时一览-图表
     */
    AjaxResult realChart(RealDataReq req);

    /**
     * 实时一览-列表
     */
    AjaxResult realList(RealDataReq req);

    /**
     * 排口在线监测列表查询
     */
    AjaxResult selectDataList(OutPutOnlineReq req);

    /**
     * 导出排口在线监测列表
     */
    void export(OutPutOnlineReq req, HttpServletResponse response);

    /**
     * 排口在线监测图表查询
     */
    AjaxResult selectDataChart(OutPutOnlineReq req);

    /**
     * 企业下的排口在线监测列表查询（多排口）
     */
    AjaxResult multipleList(OutPutOnlineReq req);

    /**
     * 企业下的排口在线监测图表查询（多排口）
     */
    AjaxResult multipleChart(OutPutOnlineReq req);

    /**
     * 企业下的排口在线监测数据报表查询
     */
    AjaxResult reportList(OutPutOnlineReq req);

    /**
     * 导出企业下的排口在线监测数据报表查询
     */
    void exportReport(OutPutOnlineReq req, HttpServletResponse response);
}

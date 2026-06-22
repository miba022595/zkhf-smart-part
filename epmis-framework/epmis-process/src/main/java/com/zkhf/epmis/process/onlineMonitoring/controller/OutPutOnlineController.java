package com.zkhf.epmis.process.onlineMonitoring.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.onlineMonitoring.domain.OutPutOnlineReq;
import com.zkhf.epmis.process.onlineMonitoring.domain.RealDataReq;
import com.zkhf.epmis.process.onlineMonitoring.service.OutPutOnlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 排口在线监测 Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/onlineMonitoring/output")
public class OutPutOnlineController {

    private OutPutOnlineService outPutOnlineService;
    @Autowired
    public void setOutPutOnlineService(OutPutOnlineService outPutOnlineService) {
        this.outPutOnlineService = outPutOnlineService;
    }

    /**
     * 实时一览-图表
     */
    @PostMapping("/realChart")
    public AjaxResult realChart(@RequestBody(required = false) RealDataReq req) {
        return outPutOnlineService.realChart(req);
    }

    /**
     * 实时一览-列表
     */
    @PostMapping("/realList")
    public AjaxResult realList(@RequestBody(required = false) RealDataReq req) {
        return outPutOnlineService.realList(req);
    }

    /**
     * 排口在线监测列表查询
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) OutPutOnlineReq req) {
        return outPutOnlineService.selectDataList(req);
    }

    /**
     * 导出排口在线监测列表
     * 按模板导出
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) OutPutOnlineReq req, HttpServletResponse response) {
        outPutOnlineService.export(req, response);
    }

    /**
     * 排口在线监测图表查询
     */
    @PostMapping("/chart")
    public AjaxResult chart(@RequestBody(required = false) OutPutOnlineReq req) {
        return outPutOnlineService.selectDataChart(req);
    }

    /**
     * 企业下的排口在线监测列表查询（多排口）
     */
    @PostMapping("/multiple/list")
    public AjaxResult multipleList(@RequestBody(required = false) OutPutOnlineReq req) {
        return outPutOnlineService.multipleList(req);
    }

    /**
     * 企业下的排口在线监测图表查询（多排口）
     */
    @PostMapping("/multiple/chart")
    public AjaxResult multipleChart(@RequestBody(required = false) OutPutOnlineReq req) {
        return outPutOnlineService.multipleChart(req);
    }

    /**
     * 企业下的排口在线监测数据报表查询
     */
    @PostMapping("/report/list")
    public AjaxResult reportList(@RequestBody(required = false) OutPutOnlineReq req) {
        return outPutOnlineService.reportList(req);
    }
    /**
     * 导出企业下的排口在线监测数据报表
     * 按模板导出
     */
    @PostMapping("/report/exportTemplate")
    public void reportExportTemplate(@RequestBody(required = false) OutPutOnlineReq req, HttpServletResponse response) {
        outPutOnlineService.exportReport(req, response);
    }
}

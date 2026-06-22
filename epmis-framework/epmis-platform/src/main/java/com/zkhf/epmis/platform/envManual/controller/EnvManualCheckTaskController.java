package com.zkhf.epmis.platform.envManual.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTaskReq;
import com.zkhf.epmis.platform.envManual.service.EnvManualCheckTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环境手工检测任务Controller
 */
@RestController
@RequestMapping("/platform/envManualCheckTask")
public class EnvManualCheckTaskController {

    private EnvManualCheckTaskService envManualCheckTaskService;
    @Autowired
    public void setEnvManualCheckTaskService(EnvManualCheckTaskService envManualCheckTaskService) {
        this.envManualCheckTaskService = envManualCheckTaskService;
    }

    /**
     * 查询环境手工检测任务列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvManualCheckTaskReq req) {
        return envManualCheckTaskService.selectEnvManualCheckTaskList(req);
    }

    /**
     * 导出环境手工检测任务列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvManualCheckTaskReq req, HttpServletResponse response) {
        envManualCheckTaskService.exportEnvManualCheckTask(req, response);
    }

    /**
     * 修改环境手工检测任务
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvManualCheckTask info) {
        return envManualCheckTaskService.updateEnvManualCheckTask(info);
    }

    /**
     * 下载环境手工检测任务报告模板
     */
    @PostMapping("/downloadReportTemplate")
    public AjaxResult downloadReportTemplate(@RequestBody(required = false) EnvManualCheckTaskReq req, HttpServletResponse response) {
        return envManualCheckTaskService.downloadReportTemplate(req, response);
    }
}

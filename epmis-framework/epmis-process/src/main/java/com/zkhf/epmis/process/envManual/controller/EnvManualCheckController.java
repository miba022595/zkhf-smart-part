package com.zkhf.epmis.process.envManual.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckReport;
import com.zkhf.epmis.process.envManual.service.EnvManualCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 环境手工检测 Controller
 */
@RestController
@RequestMapping("/process/envManualCheck")
public class EnvManualCheckController {

    private EnvManualCheckService envManualCheckService;
    @Autowired
    public void setEnvManualCheckService(EnvManualCheckService envManualCheckService) {
        this.envManualCheckService = envManualCheckService;
    }

    /**
     * 导入环境手工检测任务报告
     */
    @PostMapping(value = "/importReportTemplate")
    public AjaxResult importReportTemplate(@RequestParam("taskIdList") List<String> taskIdList,
                                           @RequestParam("file") MultipartFile file) {
        return envManualCheckService.importReportTemplate(taskIdList, file);
    }

    /**
     * 添加环境手工检测任务报告
     */
    @PostMapping(value = "/saveReport")
    public AjaxResult saveReport(@RequestBody(required = false) EnvManualCheckReport report) {
        return envManualCheckService.saveReport(report);
    }
}

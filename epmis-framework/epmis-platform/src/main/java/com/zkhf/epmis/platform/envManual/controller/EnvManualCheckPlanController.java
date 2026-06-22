package com.zkhf.epmis.platform.envManual.controller;

import jakarta.servlet.http.HttpServletResponse;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlanReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlan;
import com.zkhf.epmis.platform.envManual.service.EnvManualCheckPlanService;

/**
 * 环境手工检测计划Controller
 */
@RestController
@RequestMapping("/platform/envManualCheckPlan")
public class EnvManualCheckPlanController {

    private EnvManualCheckPlanService envManualCheckPlanService;
    @Autowired
    public void setEnvManualCheckPlanService(EnvManualCheckPlanService envManualCheckPlanService) {
        this.envManualCheckPlanService = envManualCheckPlanService;
    }

    /**
     * 查询环境手工检测计划列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvManualCheckPlanReq req) {
        return envManualCheckPlanService.selectEnvManualCheckPlanList(req);
    }

    /**
     * 导出环境手工检测计划列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvManualCheckPlanReq req, HttpServletResponse response) {
        envManualCheckPlanService.exportEnvManualCheckPlan(req, response);
    }

    /**
     * 新增环境手工检测计划
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvManualCheckPlan info) {
        return envManualCheckPlanService.insertEnvManualCheckPlan(info);
    }

    /**
     * 修改环境手工检测计划
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvManualCheckPlan info) {
        return envManualCheckPlanService.updateEnvManualCheckPlan(info);
    }

    /**
     * 删除环境手工检测计划
     */
    @DeleteMapping("/{outPutPollId}")
    public AjaxResult remove(@PathVariable String outPutPollId) {
        return envManualCheckPlanService.deleteEnvManualCheckPlanByOutPutPollId(outPutPollId);
    }
}

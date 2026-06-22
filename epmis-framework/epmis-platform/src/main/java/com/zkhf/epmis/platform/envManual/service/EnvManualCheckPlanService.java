package com.zkhf.epmis.platform.envManual.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlan;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlanReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环境手工检测计划Service接口
 */
public interface EnvManualCheckPlanService {

    /**
     * 查询环境手工检测计划列表
     */
    AjaxResult selectEnvManualCheckPlanList(EnvManualCheckPlanReq req);

    /**
     * 导出环境手工检测计划列表
     */
    void exportEnvManualCheckPlan(EnvManualCheckPlanReq req, HttpServletResponse response);

    /**
     * 新增环境手工检测计划
     */
    AjaxResult insertEnvManualCheckPlan(EnvManualCheckPlan info);

    /**
     * 修改环境手工检测计划
     */
    AjaxResult updateEnvManualCheckPlan(EnvManualCheckPlan info);

    /**
     * 删除环境手工检测计划信息
     */
    AjaxResult deleteEnvManualCheckPlanByOutPutPollId(String outPutPollId);
}

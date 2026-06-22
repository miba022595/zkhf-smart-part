package com.zkhf.epmis.platform.envManual.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlan;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTaskReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 环境手工检测任务Service接口
 */
public interface EnvManualCheckTaskService {

    /**
     * 初始化任务
     */
    void initTask(Integer oldStatus, EnvManualCheckPlan info);

    /**
     * 查询环境手工检测任务列表
     */
    AjaxResult selectEnvManualCheckTaskList(EnvManualCheckTaskReq req);

    /**
     * 导出环境手工检测任务列表
     */
    void exportEnvManualCheckTask(EnvManualCheckTaskReq req, HttpServletResponse response);

    /**
     * 修改环境手工检测任务
     */
    AjaxResult updateEnvManualCheckTask(EnvManualCheckTask info);

    /**
     * 下载环境手工检测任务报告模板
     */
    AjaxResult downloadReportTemplate(EnvManualCheckTaskReq req, HttpServletResponse response);

    /**
     * 查询环境手工检测任务列表
     */
    List<EnvManualCheckTask> selectEnvManualCheckTaskForReport(List<String> taskIdList);

    /**
     * 环境手工检测任务报告导入
     */
    void batchUpdateEnvManualCheckTask(List<EnvManualCheckTask> taskList);
}

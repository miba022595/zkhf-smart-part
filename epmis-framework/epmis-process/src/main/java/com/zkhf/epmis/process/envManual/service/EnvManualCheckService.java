package com.zkhf.epmis.process.envManual.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckReport;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 环境手工检测任务Service接口
 */
public interface EnvManualCheckService {

    /**
     * 导入环境手工检测任务报告
     */
    AjaxResult importReportTemplate(List<String> taskIdList, MultipartFile file);

    /**
     * 添加环境手工检测任务报告
     */
    AjaxResult saveReport(EnvManualCheckReport report);
}

package com.zkhf.epmis.platform.homePage.service;

import com.zkhf.epmis.core.domain.AjaxResult;

/**
 * 首页 Service接口
 */
public interface HomePageService {

    /**
     * 驾驶舱大屏-环境治理设施统计
     */
    AjaxResult cockpitEnvGovernanceStatistics(String entCode);

    /**
     * 驾驶舱大屏-环境监测设施统计
     */
    AjaxResult cockpitEnvMonitorStatistics(String entCode);

}

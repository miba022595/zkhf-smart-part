package com.zkhf.epmis.platform.homePage.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.homePage.service.HomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器 Controller
 */
@RestController
@RequestMapping("/platform/homePage")
public class HomePageController {

    private HomePageService homePageService;
    @Autowired
    public void setOutPutHourService(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    /**
     * 驾驶舱大屏-环境治理设施统计
     */
    @GetMapping("/cockpit/envGovernanceStatistics")
    public AjaxResult cockpitEnvGovernanceStatistics(String entCode) {
        return homePageService.cockpitEnvGovernanceStatistics(entCode);
    }

    /**
     * 驾驶舱大屏-环境监测设施统计
     */
    @GetMapping("/cockpit/envMonitorStatistics")
    public AjaxResult cockpitEnvMonitorStatistics(String entCode) {
        return homePageService.cockpitEnvMonitorStatistics(entCode);
    }
}


package com.zkhf.epmis.process.homePage.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.homePage.service.HomePageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器 Controller
 */
@RestController
@RequestMapping("/process/homePage")
public class HomePageController {

    private HomePageService homePageService;
    @Autowired
    public void setOutPutHourService(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    /**
     * 首页排放量列表
     */
    @GetMapping("/emissions/list")
    public AjaxResult homePageEmissionsList() {
        return homePageService.homePageEmissionsList();
    }

    /**
     * 首页实时数据列表
     */
    @GetMapping("/realData/list")
    public AjaxResult homePageRealDataList(String outPutId) {
        return homePageService.homePageRealDataList(outPutId);
    }

    /**
     * 驾驶舱大屏-24h厂区小时数据趋势
     */
    @GetMapping("/cockpit/24hTrend")
    public AjaxResult cockpit24hPlantTrend(String entCode, Integer outPutType, String outPutId, String pollutantCode) {
        return homePageService.cockpit24hPlantTrend(entCode, outPutType, outPutId, pollutantCode);
    }

    /**
     * 驾驶舱大屏-报警统计（当月、当日、当年） 默认查当日的
     */
    @GetMapping("/cockpit/alarmStatistics")
    public AjaxResult cockpitAlarmStatistics(String entCode, Integer queryType) {
        return homePageService.cockpitAlarmStatistics(entCode, queryType);
    }

    /**
     * 驾驶舱大屏-排放量列表
     */
    @GetMapping("/cockpit/emissions")
    public AjaxResult cockpitEmissions(String entCode, Integer emissionYear) {
        return homePageService.cockpitEmissions(entCode, emissionYear);
    }

    /**
     * 首页标识情况统计（当日、当周、当月、当年）
     */
    @GetMapping("/signInfo")
    public AjaxResult signInfo(Integer queryType) {
        return homePageService.signInfo(queryType);
    }
}


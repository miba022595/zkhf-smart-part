package com.zkhf.epmis.process.homePage.service;

import com.zkhf.epmis.core.domain.AjaxResult;

/**
 * 首页 Service接口
 */
public interface HomePageService {

    /**
     * 首页排放量列表
     */
    AjaxResult homePageEmissionsList();

    /**
     * 首页实时数据获取
     */
    AjaxResult homePageRealDataList(String outPutId);

    /**
     * 驾驶舱大屏-24h厂区小时数据趋势
     */
    AjaxResult cockpit24hPlantTrend(String entCode, Integer outPutType, String outPutId, String pollutantCode);

    /**
     * 驾驶舱大屏-报警统计（当月、当日、当年） 默认查当日的
     */
    AjaxResult cockpitAlarmStatistics(String entCode, Integer queryType);

    /**
     * 驾驶舱大屏-排放量列表
     */
    AjaxResult cockpitEmissions(String entCode, Integer emissionYear);

    /**
     * 首页标识情况统计
     */
    AjaxResult signInfo(Integer queryType);

}

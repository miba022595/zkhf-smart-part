package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排口在线检测报表数据消息
 */
@Data
public class OutPutOnlineReportData {

    /** 检测时间(查询时设置格式) */
    @JsonIgnore
    private LocalDateTime monitorTime;
    private String dataTime;

    /**
     * 污染信息
     */
    @JsonIgnore
    private String dataInfoStr;
    private JSONObject dataMap;
}
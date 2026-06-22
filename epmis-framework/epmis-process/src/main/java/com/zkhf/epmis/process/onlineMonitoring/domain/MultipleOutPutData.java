package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

/**
 * 多排口查询后的数据
 */
@Data
public class MultipleOutPutData {

    /** 排口主键id */
    private String outPutId;

    /** 检测时间(查询时设置格式) */
    private String monitorTime;

    /** 污染信息 */
    @JsonIgnore
    private String dataInfoStr;
    private Map<String, Object> dataMap;
}

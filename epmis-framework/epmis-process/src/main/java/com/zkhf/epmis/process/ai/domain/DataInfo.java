package com.zkhf.epmis.process.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 监测数据
 */
@Data
public class DataInfo {

    @JsonIgnore
    private String tableName;
    @JsonIgnore
    private String dataInfoStr;

    /** 检测时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitorTime;

    /** 污染信息 */
    private Map<String, Object> dataMap;
}

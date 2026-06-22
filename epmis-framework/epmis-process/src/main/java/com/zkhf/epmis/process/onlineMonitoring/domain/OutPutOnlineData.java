package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 排口在线检测数据消息
 */
@Data
public class OutPutOnlineData {
    /** 该时刻是否有报警：0：正常；1：发生报警 */
    public static final Integer normal = 0;
    /** 该时刻是否有报警：0：正常；1：发生报警 */
    public static final Integer alarm = 1;

    /** 主键id */
    private String outId;

    /**
     * 数据类型：
     * {@link DataTypeEnum}
     */
    private Integer dataType;

    /** 检测时间(查询时设置格式) */
    private String monitorTime;

    /** 检测时间(查询时设置格式) */
    @JsonIgnore
    private LocalDateTime monitorDate;

    /**
     * 污染信息
     */
    @JsonIgnore
    private String dataInfoStr;
    private Map<String, Object> dataMap;
}
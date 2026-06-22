package com.zkhf.epmis.process.alarm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import lombok.Data;

import java.util.Map;

/**
 * 数据报警统计信息
 */
@Data
public class DurAlarmCount {

    /**
     * 主键id
     */
    private Integer totalCount;

    /**
     * 未解除
     */
    private Integer activeCount;

    /**
     * 已解除
     */
    private Integer resolvedCount;

    /**
     * 未处理
     */
    private Integer pendingCount;

    /**
     * 已处理
     */
    private Integer completedCount;

    /**
     * 报警类型
     * {@link AlarmDetailTypeEnum}
     */
    @JsonIgnore
    private Integer alarmType;

    /**
     * 报警类型统计的报警数量
     */
    private Map<Integer, Integer> alarmTypeCountMap;
}
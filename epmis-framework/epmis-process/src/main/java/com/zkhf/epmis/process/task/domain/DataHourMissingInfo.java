package com.zkhf.epmis.process.task.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小时数据缺失表
 */
@Data
@Builder
public class DataHourMissingInfo {

    /**
     * 排口id
     */
    private String outPutId;
    
    /**
     * 污染物监测时间(缺数时间)
     */
    private LocalDateTime monitorTime;
    
    /**
     * 污染因子编码，小时数据单个污染物缺失时为code列表，逗号分割
     */
    private String pollutantCode;

    /**
     * 报警类型：1：小时数据整体缺失；2：小时数据单个污染因子缺失
     */
    private Integer alarmType;
}
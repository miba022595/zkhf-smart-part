package com.zkhf.epmis.process.alarm.domain;

import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import lombok.Builder;
import lombok.Data;

/**
 * 小时数据超标报警
 */
@Data
@Builder
public class OutPutStatus {

    /**
     * 排口id
     */
    private String outPutId;

    /**
     * 排口状态
     * 参见 {@link OutPutStatusEnum}
     */
    private Integer outPutStatus;

    /**
     * 报警级别
     * {@link AlarmDetailTypeEnum}
     */
    private Integer alarmLevel;
}
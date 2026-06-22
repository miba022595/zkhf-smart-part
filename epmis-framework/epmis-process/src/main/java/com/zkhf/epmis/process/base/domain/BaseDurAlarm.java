package com.zkhf.epmis.process.base.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 持续报警的实体信息
 */
@Data
public class BaseDurAlarm {

    /**
     * 主键id
     */
    private String alarmId;

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 污染因子编码
     */
    private String pollutantCode;

    /**
     * 数据类型
     * 参见 {@link DataTypeEnum}
     */
    private Integer dataType;

    /**
     * 报警类型
     * {@link AlarmDetailTypeEnum}
     */
    private Integer alarmType;

    /**
     * 报警发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;

    /**
     * 报警状态，0未解除；1已解除
     */
    private Integer alarmStatus;

    /**
     * 报警详情
     */
    private String alarmMsg;

}
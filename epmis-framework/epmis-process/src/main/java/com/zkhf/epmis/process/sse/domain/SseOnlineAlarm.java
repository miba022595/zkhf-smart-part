package com.zkhf.epmis.process.sse.domain;

import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SseOnlineAlarm {

    /**
     * 关联企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 排放口编码
     */
    private String outPutCode;

    /**
     * 排放口名称
     */
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
    private String outPutTypeDesc;

    /**
     * 数据类型
     * 参见 {@link DataTypeEnum}
     */
    private Integer dataType;
    private String dataTypeDesc;

    /**
     * 报警类型
     * 参见 {@link AlarmDetailTypeEnum}
     */
    private Integer alarmType;
    private String alarmTypeDesc;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 推送的其他消息
     */
    private String message;
}

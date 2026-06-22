package com.zkhf.epmis.process.ai.domain;

import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.DataEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

/**
 * ai请求消息
 */
@Data
public class AiDataReq {

    /** 企业名称 */
    private String entName;

    /** 排口名称 */
    private String outPutName;

    /**
     * 查询数据类型
     * 参见 {@link DataEnum}
     */
    private String dataEnum;

    /** 查询开始时间 */
    private String beginTime;

    /** 查询结束时间 */
    private String endTime;

    /** 数据类型 {@link com.zkhf.epmis.core.enums.DataTypeEnum} */
    private Integer dataType;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 报警类型
     * 参见 {@link AlarmDetailTypeEnum}
     */
    private Integer alarmType;

    /**
     * 报警状态，0未解除；1已解除
     */
    private Integer alarmStatus;

    /**
     * 处理状态，0未处理；1已处理
     */
    private Integer dealStatus;
}

package com.zkhf.epmis.process.alarm.domain;

import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 数据报警请求体
 */
@Data
public class DurAlarmReq {

    /**
     * 权限管理
     */
    private String entCode;
    private String region;
    private List<String> entCodes;

    /**
     * 排口id
     */
    private List<String> outPutIdList;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /** 查询开始时间 */
    private String beginTime;

    /** 查询结束时间 */
    private String endTime;

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

    /* *******************数据校验后的查询参数，代替请求中的 outPutIdList、pollCodeList ********* */
    /**
     * 排口信息暂存
     */
    private Map<String, OutPutInfo> outMap;
}
package com.zkhf.epmis.process.task.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排口污染物数据信息
 */
@Data
public class PollDataInfo {

    /**
     * 数据主键id
     */
    private String outId;

    /**
     * 数据项
     */
    private String dataInfo;

    /**
     * 污染物监测时间
     */
    private LocalDateTime monitorTime;
}
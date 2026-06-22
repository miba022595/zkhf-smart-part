package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.zkhf.epmis.core.enums.DataEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排口在线检测请求消息
 */
@Data
public class OutPutOnlineReq {

    /** 排口主键id */
    private String outPutId;

    /**
     * 查询数据类型
     * 参见 {@link DataEnum}
     */
    private String dataEnum;

    /** 查询开始时间 */
    private String beginTime;

    /** 查询结束时间 */
    private String endTime;

    /* ******** 多排口查询参数 ********** */
    /** 排口主键id */
    private List<String> outPutIds;

    /** 查询时间 */
    private String queryTime;
    /** 查询边距，依据不同类型表示前后几分钟、几小时、几天 */
    private Integer queryMargin;

    /* ******** 排口列表分页查询参数 ********** */

    /** 查询的表名称 */
    private String tableName;

    /** 数据类型 {@link com.zkhf.epmis.core.enums.DataTypeEnum} */
    private Integer dataType;

    /** 查询开始时间 */
    private LocalDateTime start;

    /** 查询结束时间 */
    private LocalDateTime end;

    /** 当前页的第一条，取上一页用 */
    private String outPutIdF;

    /** 当前页的最后一条，取下一页用 */
    private String outPutIdE;

    private Integer pageSize;

    /** 跳页时使用，偏移量获取 */
    private Long offset;

    /* ******** 排口数据报表查询参数 ********** */

    /**
     * 查询时间段数据类型
     * 参见 {@link com.zkhf.epmis.core.enums.TimePeriodEnum}
     */
    private String timePeriodEnum;

}

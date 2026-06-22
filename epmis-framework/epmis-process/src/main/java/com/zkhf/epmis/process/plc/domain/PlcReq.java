package com.zkhf.epmis.process.plc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * PLC数据查询请求类
 */
@Data
public class PlcReq {

    /** 企业编码 */
    private String entCode;

    /* ******************历史数据查询参数********************* */
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate start;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;

    /** 当前页第一条ID */
    private String firstId;

    /** 当前页第一条数据的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstReport;

    /** 当前页最后一条ID */
    private String lastId;

    /** 当前页最后一条数据的时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastReport;

    /** 翻页方向 true:下一页 false:上一页 */
    private boolean down;

    /** 每页大小 */
    private Integer pageSize;
}
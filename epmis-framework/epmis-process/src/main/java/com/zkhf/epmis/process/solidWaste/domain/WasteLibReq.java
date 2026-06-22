package com.zkhf.epmis.process.solidWaste.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固废库存管理请求对象
 */
@Data
public class WasteLibReq {

    /** 企业编码 */
    private List<String> entCodes;

    /** 固废种类id */
    private String categoryId;

    /** 查询开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 查询结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** left为1时表示查有剩余量的数据 */
    private Integer left;

    /** 固废分类id树 */
    private String wasteDictId;

    /** 查询年份，默认当年 */
    private Integer year;
}

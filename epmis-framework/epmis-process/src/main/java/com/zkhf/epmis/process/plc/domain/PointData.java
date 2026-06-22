package com.zkhf.epmis.process.plc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 解析后的点位数据VO
 */
@Data
public class PointData {

    /** 点位主键主键ID */
    private Long pointId;

    /** 上报时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /** 原始十六进制值 */
    private String rawValueHex;

    /** 转换后的值 */
    private Object convertedValue;

    /** 显示值 */
    private String displayValue;
}

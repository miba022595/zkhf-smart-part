package com.zkhf.epmis.process.plc.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * PLC监测数据实体类
 */
@Data
public class PlcRawData {

    /** 主键id（ulid） */
    private String id;

    /** 单元ID */
    private String type;

    /** 十六进制数据 */
    private String data;

    /** 接收时间 */
    private LocalDateTime reportTime;
}

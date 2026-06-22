package com.zkhf.epmis.process.plc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业PLC设备点位信息实体类
 */
@Data
public class PlcInfo {

    /** 自增主键ID */
    private Long id;

    /** PLC单元编号(1-255) */
    private Integer unitId;

    /** 排序号(显示顺序) */
    private Integer sortOrder;

    /** 点位名称/描述 */
    private String pointName;

    /** 点位类型(1:DI数字输入 2:DO数字输出 3:AI模拟输入 4:AO模拟输出 5:寄存器) */
    private Integer pointType;
    private String pointTypeDesc;

    /** 数据类型(1:bool 2:int16 3:uint16 4:int32 5:uint32 6:float32 7:float64) */
    private Integer dataType;
    private String dataTypeDesc;

    /** PLC绝对地址（硬件地址） */
    private Integer address;

    /** Modbus寄存器地址（通信地址） */
    private Integer registerAddress;

    /** 转换系数（4位小数） */
    private BigDecimal coefficient;

    /** 计量单位 */
    private String unit;

    /** 量程最小值（6位小数） */
    private BigDecimal minValue;

    /** 量程最大值（6位小数） */
    private BigDecimal maxValue;

    /** 小数精度(0-6) */
    private Integer precision;

    /** 状态(0:禁用 1:启用) */
    private Integer status;

    /** 描述/备注 */
    private String description;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
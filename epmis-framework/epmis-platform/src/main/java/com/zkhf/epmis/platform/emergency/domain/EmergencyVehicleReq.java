package com.zkhf.epmis.platform.emergency.domain;

import lombok.Data;

import java.util.List;

/**
 * 应急车辆查询请求对象。
 * 用于列表查询和导出筛选，同时承载数据权限范围内的企业编码集合。
 */
@Data
public class EmergencyVehicleReq {
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
    /**
     * 车牌号关键字
     */
    private String plateNo;
    /**
     * 车辆状态原始入参
     */
    private String vehicleStatus;
    /**
     * 车辆状态整型值
     */
    private Integer vehicleStatusInt;
}

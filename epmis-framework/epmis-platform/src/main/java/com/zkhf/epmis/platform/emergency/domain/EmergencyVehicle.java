package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应急车辆实体。
 * 对应企业应急车辆基础信息，同时承载企业名称等查询回显字段。
 */
@Data
public class EmergencyVehicle {
    /**
     * 车辆ID
     */
    private String vehicleId;
    /**
     * 企业编码
     */
    private String entCode;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 企业微信通知配置
     */
    private String weComMsg;
    /**
     * 车牌号
     */
    private String plateNo;
    /**
     * 车辆类型
     */
    private String vehicleType;
    /**
     * 驾驶员
     */
    private String driverName;
    /**
     * 驾驶员电话
     */
    private String driverPhone;
    /**
     * 停放位置
     */
    private String parkPlace;
    /**
     * 车辆状态：0-可用，1-维修中
     */
    private Integer vehicleStatus;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

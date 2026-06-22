package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.platform.enums.DeviceLifeUnitEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备信息对象 t_device_info
 */
@Data
public class DeviceInfo {

    /**
     * 设备mn编号
     */
    private String mnNum;

    /**
     * 设备mn名称
     */
    private String mnName;

    /**
     * 品牌
     */
    private String deviceBrand;

    /**
     * 型号
     */
    private String deviceModel;

    /**
     * 设备数量
     */
    private Integer deviceQuantity;

    /**
     * 安装时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime setupTime;

    /**
     * 寿命
     */
    private Integer lifespan;

    /**
     * 寿命单位 ${@link DeviceLifeUnitEnum}
     */
    private Integer lifeUnit;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

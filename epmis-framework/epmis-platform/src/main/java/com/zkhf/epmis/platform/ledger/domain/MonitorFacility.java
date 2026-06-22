package com.zkhf.epmis.platform.ledger.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.platform.enums.DeviceLifeUnitEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监测设施台账信息
 */
@Data
public class MonitorFacility {

    /**
     * 设备mn编号
     */
    private String mnNum;

    /**
     * 设备mn名称
     */
    private String mnName;

    /**
     * 关联企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

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
     * 治理设施主键id
     */
    private String facilityId;

    /**
     * 治理设施编号
     */
    private String facilityCode;

    /**
     * 治理设施名称
     */
    private String facilityName;
}

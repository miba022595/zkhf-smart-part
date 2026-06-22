package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业生产设施/设备对象 t_ent_produce_facility
 */
@Data
public class EntProduceFacility {
    /** 设备状态：1-在用，2-停用 */
    public static final Integer equipmentStatus_ON = 1;
    public static final Integer equipmentStatus_OFF = 2;

    /** 主键id */
    private String facilityId;

    /** 所属企业 */
    private String entCode;
    private String entName;

    /** 生产设施名称 */
    private String facilityName;

    /** 生产设施编号 */
    private String facilityCode;

    /** 设备类型 */
    private String facilityType;

    /** 设备规格 */
    private String specification;

    /** 型号 */
    private String facilityModel;

    /** 制造商/供应商 */
    private String supplier;

    /** 购置时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate buyDate;

    /** 设备状态：1-在用，2-停用 */
    private Integer equipmentStatus;

    /** 设施数量 */
    private Integer facilityNumber;

    /** 计量单位 */
    private String measureUnit;

    /** 设计生产能力 */
    private String designCapacity;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 附件列表（更新时用） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

}

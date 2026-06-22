package com.zkhf.epmis.process.solidWaste.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 固废间管理对象 t_waste_room
 */
@Data
public class WasteRoom {

    /** 固废间主键id */
    private String roomId;

    /** 归属企业 */
    private String entCode;
    private String entName;
    private String entDirectorName;
    private String entDirectorPhone;
    private String entDirectorEmail;

    /** 关联排口 */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 固/危废间名称 */
    private String roomName;

    /** 贮存间编码 */
    private String roomCode;

    /** 废物类型 */
    private String wasteType;
    private String wasteTypeDesc;

    /** 贮存间类型 */
    private String roomType;
    private String roomTypeDesc;

    /** 最大存放容量(t) */
    private Double maxCapacity;

    /** 库存预警阈值(t) */
    private Double warnLimit;

    /** 当前库存(t) - 动态计算，不对应数据库字段 */
    private Double currCapacity;

    /** 面积(㎡) */
    private Double area;

    /** 是否安装摄像头（0否 1是） */
    private Integer hasCamera;

    /** 是否防渗（0否 1是） */
    private Integer hasLeakProof;

    /** 是否通风（0否 1是） */
    private Integer hasVentilation;

    /** 是否有消防设施（0否 1是） */
    private Integer hasFireControl;

    /** 是否有应急物资（0否 1是） */
    private Integer hasEmergencySupplies;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 备注 */
    private String remark;

    /**
     * 企业厂区分布图附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

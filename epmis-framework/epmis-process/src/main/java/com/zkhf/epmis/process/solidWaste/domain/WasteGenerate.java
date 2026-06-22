package com.zkhf.epmis.process.solidWaste.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 固废产生记录对象 t_waste_generate
 */
@Data
public class WasteGenerate {

    /** 主键id */
    private String id;

    /** 固废种类id */
    private String categoryId;
    private String wasteName;

    /** 固废分类 */
    private String wasteDictId;
    private String wasteCategory;
    private String wasteType;
    private String wasteCode;

    /** 暂存场所id */
    private String roomId;
    private String roomName;

    /** 产生时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime genTime;

    /** 产生量(t) */
    private Double genQty;

    /** 剩余量(t) */
    private Double remainQty;

    /** 暂存经办人id(内部账户id) */
    private Long tempOperator;
    private String tempOperatorName;
    private String tempOperatorPhone;

    /** 暂存经办人(外部人员姓名) */
    private String tempOperatorOutName;

    /** 暂存经办人(外部人员手机号) */
    private String tempOperatorOutPhone;

    /** 运送经办人id(内部账户id) */
    private Long tranOperator;
    private String tranOperatorName;
    private String tranOperatorPhone;

    /** 运送经办人(外部人员姓名) */
    private String tranOperatorOutName;

    /** 运送经办人(外部人员手机号) */
    private String tranOperatorOutPhone;

    /** 当天序号（从1开始） */
    private Integer daySeq;

    /** 系统批次号（自动生成） */
    private String batchNo;

    /** 手工批号（用户手动输入，用于兼容外部系统） */
    private String manualBatchNo;

    /** 备注 */
    private String remark;
}

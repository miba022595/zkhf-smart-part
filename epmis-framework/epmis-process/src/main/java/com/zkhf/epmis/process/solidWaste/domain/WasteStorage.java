package com.zkhf.epmis.process.solidWaste.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固废入库记录对象 t_waste_storage
 */
@Data
public class WasteStorage {

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

    /** 储存场所id */
    private String roomId;
    private String roomName;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime storageTime;

    /** 入库量(t) */
    private Double storageQty;

    /** 库存量(t) */
    private Double remainQty;

    /** 入库经办人id(内部账户id) */
    private Long storageOperator;
    private String storageOperatorName;
    private String storageOperatorPhone;

    /** 入库经办人(外部人员姓名) */
    private String storageOperatorOutName;

    /** 入库经办人(外部人员手机号) */
    private String storageOperatorOutPhone;

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

    /** 关联的列表 */
    private List<WasteFlowRel> relList;
}

package com.zkhf.epmis.process.solidWaste.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固废出库记录对象 t_waste_outbound
 */
@Data
public class WasteOutbound {

    /** 主键id */
    private String id;

    /** 出库类型，1贮存出库，2立产立清 */
    private Integer outType;

    /** 固废种类id */
    private String categoryId;
    private String wasteName;

    /** 固废分类 */
    private String wasteDictId;
    private String wasteCategory;
    private String wasteType;
    private String wasteCode;

    /** 出库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outTime;

    /** 出库量(t) */
    private Double outQty;

    /** 出库经办人id(内部账户id) */
    private Long outOperator;
    private String outOperatorName;
    private String outOperatorPhone;

    /** 出库经办人(外部人员姓名) */
    private String outOperatorOutName;

    /** 出库经办人(外部人员手机号) */
    private String outOperatorOutPhone;

    /** 运送经办人id(内部账户id) */
    private Long tranOperator;
    private String tranOperatorName;
    private String tranOperatorPhone;

    /** 运送经办人(外部人员姓名) */
    private String tranOperatorOutName;

    /** 运送经办人(外部人员手机号) */
    private String tranOperatorOutPhone;

    /** 运输单位（第三方单位） */
    private String tranUnit;
    private String tranUnitName;

    /** 车牌号 */
    private String vehicleNo;

    /** 处置单位（第三方单位） */
    private String disposalUnit;
    private String disposalUnitName;

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

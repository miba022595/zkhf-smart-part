package com.zkhf.epmis.process.solidWaste.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固废减量记录对象 t_waste_reduction
 */
@Data
public class WasteReduction {

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

    /** 处理场所id */
    private String roomId;
    private String roomName;

    /** 减量处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reductionTime;

    /** 减量量(t) */
    private Double reductionQty;

    /** 运行时长(分钟) */
    private Long duration;

    /** 减量方式 */
    private String method;

    /** 操作人员id(内部账户id) */
    private Long operator;
    private String operatorName;
    private String operatorPhone;

    /** 操作人员(外部人员姓名) */
    private String operatorOutName;

    /** 操作人员(外部人员手机号) */
    private String operatorOutPhone;

    /** 当天序号（从1开始） */
    private Integer daySeq;

    /** 系统批次号 */
    private String batchNo;

    /** 手工批号（用户手动输入，用于兼容外部系统） */
    private String manualBatchNo;

    /** 备注 */
    private String remark;

    /** 关联的列表 */
    private List<WasteFlowRel> relList;
}

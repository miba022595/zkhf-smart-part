package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

/**
 * 固废总量控制计划对象 t_waste_total_plan
 */
@Data
public class WasteTotalPlan {

    /** 固废分类id树 */
    private String wasteDictId;
    private String wasteCategory;
    private String wasteType;
    private String wasteCode;

    /** 所属企业 */
    private String entCode;
    private String entName;

    /** 关联排口 */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 年份 */
    private Integer year;

    /** 年度总量上限(t) */
    private Double annualLimit;

    /** 第一 季度上限(t) */
    private Double firstLimit;

    /** 第二 季度上限(t) */
    private Double secondLimit;

    /** 第三 季度上限(t) */
    private Double thirdLimit;

    /** 第四 季度上限(t) */
    private Double fourthLimit;

    /** 预警阈值(%) */
    private Double warnVal;

    /** 告警阈值(%) */
    private Double alarmVal;

    /** 备注 */
    private String remark;

}

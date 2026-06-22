package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 应急演练查询请求对象。
 * 用于列表和导出筛选，同时承载数据权限范围内的企业编码集合。
 */
@Data
public class EmergencyDrillReq {
    /**
     * 演练ID
     */
    private String drillId;
    /**
     * 演练名称关键字
     */
    private String drillName;
    /**
     * 演练类型关键字
     */
    private String drillType;
    /**
     * 演练日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate drillDate;
    /**
     * 关联预案ID
     */
    private String relatedPlan;
    /**
     * 演练内容关键字
     */
    private String drillContent;
    /**
     * 演练总结关键字
     */
    private String drillSummary;
    /**
     * 演练状态
     */
    private Integer drillStatus;
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
}

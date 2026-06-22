package com.zkhf.epmis.platform.emergency.domain;

import lombok.Data;

import java.util.List;

/**
 * 应急预案查询请求对象。
 * 用于列表查询和导出筛选，同时承载数据权限范围内的企业编码集合。
 */
@Data
public class EmergencyPlanReq {
    /**
     * 预案ID
     */
    private String planId;
    /**
     * 预案名称关键字
     */
    private String planName;
    /**
     * 版本号关键字
     */
    private String version;
    /**
     * 风险单元关键字
     */
    private String riskUnit;
    /**
     * 预案类型
     */
    private Integer planType;
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
}

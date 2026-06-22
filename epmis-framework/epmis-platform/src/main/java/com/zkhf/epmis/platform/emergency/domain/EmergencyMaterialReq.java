package com.zkhf.epmis.platform.emergency.domain;

import lombok.Data;

import java.util.List;

/**
 * 应急物资查询请求对象。
 * 用于列表和导出筛选，同时承载数据权限范围内的企业编码集合。
 */
@Data
public class EmergencyMaterialReq {
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
    /**
     * 物资名称关键字
     */
    private String materialName;
    /**
     * 存放地点关键字
     */
    private String storePlace;
    /**
     * 预警状态
     */
    private Integer warnStatus;
}

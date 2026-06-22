package com.zkhf.epmis.platform.emergency.domain;

import lombok.Data;

import java.util.List;

/**
 * 应急通讯录查询请求对象。
 * 用于列表查询和导出筛选，同时承载数据权限过滤后的企业编码集合。
 */
@Data
public class EmergencyContactReq {
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
    /**
     * 联系人姓名关键字
     */
    private String contactName;
    /**
     * 所属部门/小组关键字
     */
    private String deptGroup;
}

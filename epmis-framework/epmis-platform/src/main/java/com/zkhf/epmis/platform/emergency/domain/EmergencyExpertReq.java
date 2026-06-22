package com.zkhf.epmis.platform.emergency.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应急专家查询请求对象。
 * 用于列表和导出筛选，同时承载数据权限范围内的企业编码集合。
 */
@Data
public class EmergencyExpertReq {
    /**
     * 专家ID
     */
    private String expertId;
    /**
     * 专家姓名关键字
     */
    private String expertName;
    /**
     * 工作单位关键字
     */
    private String workUnit;
    /**
     * 职务职称关键字
     */
    private String positionTitle;
    /**
     * 擅长方向关键字
     */
    private String specialty;
    /**
     * 联系电话关键字
     */
    private String phone;
    /**
     * 联系邮箱关键字
     */
    private String email;
    /**
     * 备注关键字
     */
    private String remark;
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
}

package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应急通讯录实体。
 * 对应应急联系人基础信息，同时承载企业名称等查询回显字段。
 */
@Data
public class EmergencyContact {
    /**
     * 联系人主键ID
     */
    private String contactId;
    /**
     * 所属企业编码
     */
    private String entCode;
    /**
     * 所属企业名称
     */
    private String entName;
    /**
     * 企业微信通知配置
     */
    private String weComMsg;
    /**
     * 联系人姓名
     */
    private String contactName;
    /**
     * 联系人职务
     */
    private String position;
    /**
     * 所属部门/小组
     */
    private String deptGroup;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 擅长方向/职责描述
     */
    private String specialty;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

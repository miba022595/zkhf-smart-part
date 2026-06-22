package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应急专家实体。
 * 对应企业应急专家基础信息，同时承载企业回显字段。
 */
@Data
public class EmergencyExpert {
    /**
     * 专家ID
     */
    private String expertId;
    /**
     * 企业编码
     */
    private String entCode;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 企业微信通知配置
     */
    private String weComMsg;
    /**
     * 专家姓名
     */
    private String expertName;
    /**
     * 工作单位
     */
    private String workUnit;
    /**
     * 职务职称
     */
    private String positionTitle;
    /**
     * 擅长方向
     */
    private String specialty;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 联系邮箱
     */
    private String email;
    /**
     * 备注
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

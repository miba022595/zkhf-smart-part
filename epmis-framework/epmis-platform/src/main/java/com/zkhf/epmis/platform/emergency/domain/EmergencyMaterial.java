package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应急物资实体。
 * 对应企业应急物资基础信息，同时承载预警状态和附件回显字段。
 */
@Data
public class EmergencyMaterial {
    /**
     * 物资ID
     */
    private String materialId;
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
     * 物资名称
     */
    private String materialName;
    /**
     * 规格型号
     */
    private String modelSpec;
    /**
     * 存放地点
     */
    private String storePlace;
    /**
     * 数量
     */
    private Double quantity;
    /**
     * 单位
     */
    private String unit;
    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;
    /**
     * 下次复检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recheckDate;
    /**
     * 生产厂家
     */
    private String manufacturerName;
    /**
     * 厂家联系人
     */
    private String manufacturerContact;
    /**
     * 厂家联系电话
     */
    private String manufacturerPhone;
    /**
     * 管理人
     */
    private String managerName;
    /**
     * 管理人联系电话
     */
    private String managerPhone;
    /**
     * 备注
     */
    private String remark;
    /**
     * 预警状态：0-正常，1-已过期，2-即将过期
     */
    private Integer warnStatus;
    /**
     * 预警天数
     */
    private Integer warnDays;
    /**
     * 预警类型：expire-有效期，recheck-复检
     */
    private String warnType;
    /**
     * 附件ID列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
    /**
     * 附件信息列表
     */
    private List<AnnexInfo> annexList;
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

package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应急预案实体。
 * 对应企业应急预案基础信息，同时承载附件和企业回显字段。
 */
@Data
public class EmergencyPlan {
    /**
     * 预案ID
     */
    private String planId;
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
     * 预案名称
     */
    private String planName;
    /**
     * 版本号
     */
    private String version;
    /**
     * 风险单元
     */
    private String riskUnit;
    /**
     * 处置要点
     */
    private String handlePoints;
    /**
     * 预案类型：1-综合预案，2-专项预案，3-现场处置方案
     */
    private Integer planType;
    /**
     * 备注
     */
    private String remark;
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

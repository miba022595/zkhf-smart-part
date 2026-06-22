package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应急演练实体。
 * 对应企业应急演练基础信息，同时承载附件和企业回显字段。
 */
@Data
public class EmergencyDrill {
    /**
     * 演练ID
     */
    private String drillId;
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
     * 演练名称
     */
    private String drillName;
    /**
     * 演练类型
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
     * 演练内容
     */
    private String drillContent;
    /**
     * 演练总结
     */
    private String drillSummary;
    /**
     * 演练状态：0-计划中，1-已完成
     */
    private Integer drillStatus;
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

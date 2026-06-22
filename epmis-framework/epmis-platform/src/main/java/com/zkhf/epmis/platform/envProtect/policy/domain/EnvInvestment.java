package com.zkhf.epmis.platform.envProtect.policy.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 环保法规与体系管理-环保投入对象 t_env_investment
 */
@Data
public class EnvInvestment {

    /**
     * 主键id
     */
    private String investmentId;

    /**
     * 所属企业（项目所在单位）
     */
    private String entCode;
    private String entName;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 投资金额(万)
     */
    private Double investmentAmount;

    /**
     * 环保人员主键id（项目负责人）
     */
    private String proPersonId;
    private String proName;

    /**
     * 项目内容
     */
    private String investmentDesc;

    /**
     * 减排效果
     */
    private String reductionEffect;

    /**
     * 是否取得政府资金支持，1是，其他否
     */
    private Integer governmentFund;

    /**
     * 计划内部立项时间
     */
    private LocalDate pipaTime;

    /**
     * 实际内部立项时间
     */
    private LocalDate aipaTime;

    /**
     * 计划施工入厂时间
     */
    private LocalDate pceTime;

    /**
     * 实际施工入厂时间
     */
    private LocalDate aceTime;

    /**
     * 计划完成时间
     */
    private LocalDate pcTime;

    /**
     * 实际完成时间
     */
    private LocalDate afTime;

    /**
     * 计划验收时间
     */
    private LocalDate paTime;

    /**
     * 实际验收时间
     */
    private LocalDate acTime;

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

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

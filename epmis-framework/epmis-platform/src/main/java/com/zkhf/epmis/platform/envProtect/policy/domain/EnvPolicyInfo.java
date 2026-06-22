package com.zkhf.epmis.platform.envProtect.policy.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 环境政策法规信息 t_env_policy_regulation
 */
@Data
public class EnvPolicyInfo {

    /**
     * 主键id
     */
    private String policyId;

    /**
     * 所属企业
     */
    private String entCode;
    private String entName;

    /**
     * 法规标准名称
     */
    private String policyName;

    /**
     * 政策法规编号（文号）
     */
    private String policyCode;

    /**
     * 发行部门
     */
    private String issueDept;

    /**
     * 发布地区（国家/地方）
     */
    private String region;
    private String regionDesc;

    /**
     * 法规类型，字典 regulatory_type
     */
    private String policyType;
    private String policyTypeDesc;

    /**
     * 行业类别
     * "industryCategory": "94,610",
     * "industryCategoryList": ["铜矿采选", "汽柴油车整车制造"],
     * "industryCodeList": ["C1234","A12344"],
     * "industryList": [[12,79,92,94],[12,79,92,94]],
     */
    private String industryCategory;
    private List<String> industryCategoryList;
    private List<String> industryCodeList;
    private List<List<String>> industryList;

    /**
     * 环保要素，字典env_element（多选）
     */
    private String envElement;
    private String envElementDesc;

    /**
     * 管理要素，字典mange_element（多选）
     */
    private String mangeElement;
    private String mangeElementDesc;

    /**
     * 重要程度，字典sys_significance
     */
    private String significance;
    private String significanceDesc;

    /**
     * 发布日期
     */
    private LocalDate publishDate;

    /**
     * 实施日期
     */
    private LocalDate implementDate;

    /**
     * 法规状态，字典regulatory_status
     */
    private Integer status;
    private String statusDesc;

    /**
     * 适用范围
     */
    private String applicableScope;

    /**
     * 主要内容
     */
    private String mainContent;

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

package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业基础对象 t_bas_enterprise
 */
@Data
public class Enterprise {

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 上级企业编码
     */
    private String parentCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 社会统一信用代码
     */
    private String socialCreditCode;

    /**
     * 企业简称
     */
    private String shorterName;

    /**
     * 法定代表人
     */
    private String legalPerson;

    /**
     * 所在地区（地区选择）
     * id1,id2...
     */
    private String region;
    private String regionDesc;

    /**
     * 详细地址（手填）
     */
    private String address;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 企业状态, 字典enterprise_status
     */
    private String entStatus;
    private String entStatusDesc;

    /**
     * 企业规模，字典enterprise_scale
     */
    private String entScale;
    private String entScaleDesc;

    /**
     * 企业类型，字典enterprise_type
     */
    private String entType;
    private String entTypeDesc;

    /**
     * 行业类型，字典industry_type
     */
    private String industryType;
    private String industryTypeDesc;

    /**
     * 污染源类别，字典types_of_pollution_sources
     */
    private String pollutionClass;
    private String pollutionClassDesc;

    /**
     * 企业负责人姓名
     */
    private String entDirectorName;

    /**
     * 企业负责人电话
     */
    private String entDirectorPhone;

    /**
     * 企业负责人邮箱
     */
    private String entDirectorEmail;

    /**
     * 环保负责人姓名
     */
    private String envDirectorName;

    /**
     * 环保负责人电话
     */
    private String envDirectorPhone;

    /**
     * 环保负责人邮箱
     */
    private String envDirectorEmail;

    /**
     * 企业介绍
     */
    private String entIntroduction;

    /**
     * 企业微信关联信息
     */
    private String weComMsg;

    /**
     * 环保制度名称
     */
    private String envPolicyName;

    /**
     * 环保制度执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate envPolicyDate;

    /**
     * 环保制度执行级别
     */
    private String envPolicyLevel;

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
     * 企业厂区分布图附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 企业厂区分布图附件列表（查询时使用）
     */
    private List<AnnexInfo> annexList;
}

package com.zkhf.epmis.platform.envProtect.policy.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 政府资金支持对象 t_govern_fund_support
 */
@Data
public class GovernFundSupport {

    /**
     * 政府资金支持-主键id
     */
    private String supportId;

    /**
     * 企业编码
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
    private String projectContent;

    /**
     * 减排效果
     */
    private String reduceEffect;

    /**
     * 外部立项-计划时间
     */
    private LocalDate epsTime;

    /**
     * 外部立项-实际时间
     */
    private LocalDate epaTime;

    /**
     * 项目完成-计划时间
     */
    private LocalDate pcsTime;

    /**
     * 项目完成-实际时间
     */
    private LocalDate pcaTime;

    /**
     * 政府资金支持金额(万)
     */
    private Double supportAmount;

    /**
     * 下发支持资金部门
     */
    private String sendDept;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

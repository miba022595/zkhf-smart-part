package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

import java.time.LocalDate;

/**
 * 政府资金支持-计划批次对象 t_govern_fund_support_batch_plan
 */
@Data
public class SupportBatchPlan {

    /** 主键id */
    private Long id;

    /** 政府资金支持-主键id */
    private String supportId;

    /** 计划批次名称 */
    private String planName;

    /** 计划批次比例 */
    private Integer planRate;

    /** 计划下发时间 */
    private LocalDate sendTime;
}

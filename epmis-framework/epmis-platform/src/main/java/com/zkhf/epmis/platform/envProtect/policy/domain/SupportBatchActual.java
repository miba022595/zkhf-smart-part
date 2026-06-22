package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

import java.time.LocalDate;

/**
 * 政府资金支持-实际批次对象 t_govern_fund_support_batch_actual
 */
@Data
public class SupportBatchActual {

    /** 主键id */
    private Long id;

    /** 政府资金支持-主键id */
    private String supportId;

    /** 实际到账时间 */
    private LocalDate actualTime;

    /** 实际到账金额(万) */
    private Double actualAmount;
}

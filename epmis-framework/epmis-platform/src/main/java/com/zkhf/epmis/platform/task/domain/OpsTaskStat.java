package com.zkhf.epmis.platform.task.domain;

import lombok.Data;

import java.time.LocalDate;

/**
 * 运维任务对象 t_ops_task_stat
 */
@Data
public class OpsTaskStat {

    /** 统计ID */
    private String statId;

    /** 企业编码 */
    private String entCode;

    /** 排放口ID */
    private String outPutId;

    /** 关联的运维类型 */
    private String templateCode;

    /** 执行人 */
    private Long operator;

    /** 统计日期 */
    private LocalDate taskStatDate;

    /** 自动生成任务数 */
    private Integer autoTasks;

    /** 手动生成任务数 */
    private Integer manualTasks;

    /** 已完成任务数 */
    private Integer completedTasks;

    /** 合格任务数 */
    private Integer qualifiedTasks;

    /** 不合格任务数 */
    private Integer unqualifiedTasks;
}

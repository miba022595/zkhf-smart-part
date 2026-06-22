package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

/**
 * 运维任务统计
 */
@Data
public class OpsStat {

    /** 关联的运维类型 */
    private String templateCode;
    private String templateName;

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

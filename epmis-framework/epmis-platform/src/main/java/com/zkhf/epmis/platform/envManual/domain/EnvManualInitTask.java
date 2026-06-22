package com.zkhf.epmis.platform.envManual.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 环境手工检测任务生成
 */
@Data
@Builder
public class EnvManualInitTask {

    /** 检测任务主键id */
    private String taskId;

    /** 排口污染物主键id */
    private String outPutPollId;

    /** 任务日期 */
    private LocalDate taskDate;

    /** 任务状态 {@link com.zkhf.epmis.platform.envManual.enums.TaskStatusType} */
    private Integer status;

    /** 计划检测频次 {@link com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType}*/
    private Integer checkFrequency;
}

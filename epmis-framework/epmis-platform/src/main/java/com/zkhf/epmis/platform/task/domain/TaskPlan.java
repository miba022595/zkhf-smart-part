package com.zkhf.epmis.platform.task.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import lombok.Data;

import java.time.LocalDate;

/**
 * 环境手工检测计划对象 t_env_manual_check_plan
 */
@Data
public class TaskPlan {

    /** 排口污染物主键id */
    private String outPutPollId;

    /**
     * 计划首次执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstDate;

    /** 计划检测频次（1日次、2周次、3月次、4季度、5半年、6年、7两年） {@link CheckFrequencyType}*/
    private String checkFrequency;
}

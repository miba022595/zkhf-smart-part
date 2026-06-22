package com.zkhf.epmis.platform.envManual.domain;

import lombok.Data;

import java.util.List;

/**
 * 环境手工检测任务对象 t_env_manual_check_task
 */
@Data
public class EnvManualCheckTaskReq {

    /**
     * 关联企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /** 排口污染物主键id */
    private String outPutPollId;

    /** 任务状态 {@link com.zkhf.epmis.platform.envManual.enums.TaskStatusType} */
    private Integer status;

    /**
     * 任务日期-开始日期, yyyy-MM-dd
     */
    private String taskDateStart;

    /**
     * 任务日期-结束日期,yyyy-MM-dd
     */
    private String taskDateEnd;

    /** 检测单位（第三方单位） */
    private String monitorUnit;

    /** 报告编号 */
    private String reportCode;

    /**
     * 报告导入时勾选的任务列表
     */
    private List<String> taskIdList;

    /**
     * 状态列表列表
     */
    private List<Integer> statusList;

}

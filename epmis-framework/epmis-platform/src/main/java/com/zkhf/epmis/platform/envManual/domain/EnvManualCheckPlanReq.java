package com.zkhf.epmis.platform.envManual.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 环境手工检测计划对象 t_env_manual_check_plan
 */
@Data
public class EnvManualCheckPlanReq {

    /**
     * 关联企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /** 计划状态 {@link com.zkhf.epmis.platform.envManual.enums.PlanStatusType}*/
    private Integer status;

    /**
     * 计划首次执行时间-开始日期, yyyy-MM-dd
     */
    private String firstDateStart;

    /**
     * 计划首次执行时间-结束日期,yyyy-MM-dd
     */
    private String firstDateEnd;

}

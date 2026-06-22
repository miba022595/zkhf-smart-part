package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.platform.ops.enums.OpsTaskType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运维任务对象 t_ops_task
 */
@Data
public class OpsTask {

    /** 运维任务ID */
    private String taskId;

    /** 企业编码 */
    private String entCode;
    private String entName;

    /** 排放口ID */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 关联的运维类型 */
    private String templateCode;
    private String templateName;

    /** 任务类型（1：自动生成的任务，2：手动生成的任务） */
    private Integer taskType;

    /** 任务状态 {@link OpsTaskType} */
    private Integer taskStatus;

    /** 任务执行情况描述 */
    private String taskDesc;

    /** 计划执行日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /** 提前推送提醒(天) */
    private Integer earlyDays;

    /** 执行人 */
    private Long operator;
    private String operatorName;
    private String operatorNick;

    /** 分配时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignTime;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private Long createId;
    private String createName;
    private String createNick;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 最新的运维记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordDate;

}

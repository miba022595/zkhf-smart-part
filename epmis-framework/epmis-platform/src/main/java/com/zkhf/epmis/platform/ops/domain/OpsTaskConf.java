package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运维任务配置对象 t_ops_task_conf
 */
@Data
public class OpsTaskConf {

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

    /** 任务开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /** 是否启动任务（1：启动，0：不启动） */
    private Integer enabled;

    /** 执行周期类型（1：日，2：周，3：月，4：季度，6：年） */
    private Integer cycleType;

    /** 周期数值（如：每3天，每2周等） */
    private Integer cycleValue;

    /** 提前推送提醒(天) */
    private Integer earlyDays;

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

}

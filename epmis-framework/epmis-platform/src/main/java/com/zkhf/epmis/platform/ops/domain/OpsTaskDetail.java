package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

/**
 * 运维任务配置-运维内容对象
 */
@Data
public class OpsTaskDetail {

    /** 运维内容ID（主键） */
    private String templateDetailId;

    /** 执行周期类型（1：日，2：周，3：月，4：季度，6：年） */
    private Integer cycleType;

    /** 周期数值（如：每3天，每2周等） */
    private Integer cycleValue;

    /** 是否命中执行任务（true：命中，false、null：未命中） */
    private Boolean execute;

}

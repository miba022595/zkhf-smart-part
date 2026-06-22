package com.zkhf.epmis.process.task.domain;

import com.zkhf.epmis.core.enums.DataTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业数据有效率信息表
 * 有效传输率=有效率*传输率
 * 摘自：《污染物排放自动监测设备标记规则》.pdf
 */
@Data
public class EffectiveTransTask {

    /**
     * 有效率主键id
     * yyyyMMddHHmmss+dataType
     */
    private String effId;

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 检测的数据类型
     * 参见 {@link DataTypeEnum}
     */
    private Integer dataType;

    /**
     * 传输实收量
     */
    private int realTrans;
    /**
     * 传输应收量
     */
    private int mustTrans;

    /**
     * 有效实收量
     */
    private int realValid;
    /**
     * 有效应收量
     */
    private int mustValid;

    /**
     * 污染物监测时间，yyyy-MM-dd
     */
    private LocalDateTime monitorTime;
}
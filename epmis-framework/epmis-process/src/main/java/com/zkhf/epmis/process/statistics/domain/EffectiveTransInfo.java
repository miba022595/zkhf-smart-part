package com.zkhf.epmis.process.statistics.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业数据有效率信息表
 * 有效传输率=有效率*传输率
 * 摘自：《污染物排放自动监测设备标记规则》.pdf
 */
@Data
public class EffectiveTransInfo {

    public static final Integer NORMAL = 0;
    public static final Integer ALARM = 1;

    /**
     * 有效率主键id
     * 依据(企业编码+排口编码+污染物类型+数据来源（小时、日）) 拼接后sha256前16位 + 时间
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
    private Integer realTrans;
    /**
     * 传输应收量
     */
    private Integer mustTrans;

    /**
     * 有效实收量
     */
    private Integer realValid;
    /**
     * 有效应收量
     */
    private Integer mustValid;

    /**
     * 污染物监测时间，yyyy-MM-dd
     */
    private String monitorDate;
    @JsonIgnore
    private LocalDateTime monitorTime;

    /**
     * 数据传输率
     */
    private Float transRate;
    /**
     * 数据传输率报警，0正常，1报警
     */
    private Integer transAlarm;
    /**
     * 数据有效率
     */
    private Float validRate;
    /**
     * 数据有效率报警，0正常，1报警
     */
    private Integer validAlarm;

    /**
     * 数据有效传输率
     */
    private Float effTranRate;
    /**
     * 数据有效传输率报警，0正常，1报警
     */
    private Integer effTranAlarm;

    /**
     * 企业编码
     */
    private String entCode;
    private String entName;

    /**
     * 排口编码
     */
    private String outPutCode;
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
    private String outPutTypeDesc;
}
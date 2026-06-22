package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业排口污染物信息对象 t_ent_output_pollutant
 */
@Data
public class EntOutputPollutant {

    /**
     * 排口污染物主键id
     */
    private String outPutPollId;

    /**
     * 关联排口主键id
     */
    private String outPutId;

    /**
     * 排口关联污染物code
     */
    private String pollutantCode;
    private String pollutantNameCn;
    private String pollutantNameEn;

    /**
     * 排序码
     */
    private Integer pollutantSort;

    /**
     * 是否零值报警 0：否；1：是
     */
    private Integer isZeroAlarm;

    /**
     * 是否连续值报警：0：否；1：是
     */
    private Integer isContinuityAlarm;

    /**
     * 是否负值报警 0：否；1：是
     */
    private Integer isNegativeAlarm;

    /**
     * 月段污染物排放限值
     */
    private String monthlyLimitValue;

    /**
     * 超标上限--当alarmType=1时区间上限；当为3时上线报警的最大值
     */
    private Double overMaxvalue;

    /**
     * 超标下限--当alarmType=1时区间下限；当为4时上限报警的最小值
     */
    private Double overMinvalue;

    /**
     * 计量单位，为空则取污染物表里的配置
     */
    private String rtdUnit;

    /**
     * 累计单位，为空则取污染物表里的配置
     */
    private String couUnit;

    /**
     * 小数点位数，默认3
     */
    private Integer decimalPlaces;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 监测因子项，选择的逗号拼接
     */
    private String monFactor;
}
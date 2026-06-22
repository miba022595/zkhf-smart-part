package com.zkhf.epmis.platform.ent.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 在线检测数据动态表头列表
 * OLM：on-line monitoring
 */
@Data
public class PollDataAutoHead {

    /**
     * 污染物编码
     */
    private String pollutantCode;

    /**
     * 报警类型：0：无报警；1：区间值报警；2：上限报警；3：下限报警；
     */
    private String alarmType;

    /**
     * 是否零值报警 0：否；1：是
     */
    private String isZeroAlarm;

    /**
     * 是否连续值报警：0：否；1：是
     */
    private String isContinuityAlarm;

    /**
     * 月段污染物排放限值
     */
    private String monthlyLimitValue;

    /**
     * 超标上限--当alarmType=1时区间上限；当为3时上限报警的最大值
     */
    private BigDecimal overMaxValue;

    /**
     * 超标下限--当alarmType=1时区间下限；当为4时上限报警的最小值
     */
    private BigDecimal overMinValue;

    /**
     * 选择的监测因子
     */
    private String monFactor;

    /**
     * 污染因子名称--英文
     */
    private String pollutantNameEn;

    /**
     * 污染因子名称--中文
     */
    private String pollutantNameCn;

    /**
     * 污染因子单位--英文
     */
    private String pollutantUnitEn;

    /**
     * 污染因子单位--中文
     */
    private String pollutantUnitCn;

    /**
     * 排放量单位--中文
     */
    private String unitPfCn;

    /**
     * 排放量单位--英文
     */
    private String unitPfEn;
}

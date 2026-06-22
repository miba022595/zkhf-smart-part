package com.zkhf.epmis.process.base.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 排口污染物基础信息
 */
@Data
public class OutPutPollInfo {

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 排口编码
     */
    private String outPutCode;

    /**
     * 排口名称
     */
    private String outPutName;

    /**
     * 检测设备mn号
     */
    private String mnNum;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 污染物编码
     */
    private String pollutantCode;

    /**
     * 污染物中文名
     */
    private String pollutantNameCn;

    /**
     * 污染物英文名
     */
    private String pollutantNameEn;

    /**
     * 污染物单位中文名
     */
    private String pollutantUnitCn;

    /**
     * 污染物单位英文名
     */
    private String pollutantUnitEn;

    /**
     * 月排放量
     */
    private String monthlyLimitValue;

    /**
     * 截止到当前月的排放量（包含）
     */
    private BigDecimal toNowLimitValue;

    /**
     * 监测因子项列表
     */
    private String monFactor;

    /**
     * 是否连续值报警：0：否；1：是
     */
    private Integer isContinuityAlarm;

    /**
     * 是否零值报警 0：否；1：是
     */
    private Integer isZeroAlarm;

    /**
     * 是否负值报警 0：否；1：是
     */
    private Integer isNegativeAlarm;

    /**
     * 超标上限--当alarmType=1时区间上限；当为3时上线报警的最大值
     */
    private BigDecimal overMaxValue;

    /**
     * 超标下限--当alarmType=1时区间下限；当为4时上限报警的最小值
     */
    private BigDecimal overMinValue;
}

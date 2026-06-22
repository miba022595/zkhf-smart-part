package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum AlarmDetailTypeEnum {
    DEFAULT(0, 999,""),

    /************* 预警枚举 ***************/
    WARN_LARGE(-1, 101, "上限预警"),
    WARN_SMALL(-2, 102, "下限预警"),
    WARN_ZERO(-3, 103, "零值预警"),
    WARN_CONSTANT(-4, 104, "恒值预警"),
    WARN_NEGATIVE(-5, 105, "负值预警"),
    WARN_TOTAL_EMISSION(-6, 106, "排放总量超标预警"),
    WARN_NET_ERR(-7, 107, "联网异常预警"),
    WARN_HOUR_MISS(-8, 108, "小时数据缺失预警"),
    WARN_HOUR_IMPERFECT(-9, 109, "小时数据不完整预警"),

    /************* 报警枚举 ***************/
    ALARM_LARGE(1, 1, "上限报警"),
    ALARM_SMALL(2, 2, "下限报警"),
    ALARM_ZERO(3, 3, "零值报警"),
    ALARM_CONSTANT(4, 4, "恒值报警"),
    ALARM_NEGATIVE(5, 5, "负值报警"),
    ALARM_TOTAL_EMISSION(6, 6, "排放总量超标报警"),// 一个企业一天一条
    ALARM_NET_ERR(7, 7, "联网异常报警"),
    ALARM_HOUR_MISS(8, 8, "小时数据缺失报警"),
    ALARM_HOUR_IMPERFECT(9, 9, "小时数据不完整报警"),
    ;
    public static final String ALARM_ZERO_STR = ALARM_ZERO.code.toString();
    public static final String ALARM_CONSTANT_STR = ALARM_CONSTANT.code.toString();
    public static final String WARN_ZERO_STR = WARN_ZERO.code.toString();
    public static final String WARN_CONSTANT_STR = WARN_CONSTANT.code.toString();

    public final Integer code;
    /** 报警等级，数字越小，报警级别越高 */
    public final Integer level;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, AlarmDetailTypeEnum> CODE_TO_NAME =
            Arrays.stream(AlarmDetailTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e
                    ));

    AlarmDetailTypeEnum(Integer code, Integer level, String name) {
        this.code = code;
        this.level = level;
        this.name = name;
    }

    public static AlarmDetailTypeEnum getNameByCode(Integer code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return DEFAULT;
    }
}

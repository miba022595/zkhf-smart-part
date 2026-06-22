package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum AlarmTypeEnum {
    ALARM_GREEN("green", "绿色正常"),
    ALARM_YELLOW("yellow", "黄色提醒"),
    ALARM_ORANGE("orange", "橙色预警"),
    ALARM_RED("red", "红色报警"),
    ;

    public final String code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String> CODE_TO_NAME =
            Arrays.stream(AlarmTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    AlarmTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return "";
    }

    public static boolean notContainsCode(String code) {
        return !containsType(code);
    }

    public static boolean containsType(String code) {
        if (null == code)
            return false;
        return CODE_TO_NAME.containsKey(code);
    }
}

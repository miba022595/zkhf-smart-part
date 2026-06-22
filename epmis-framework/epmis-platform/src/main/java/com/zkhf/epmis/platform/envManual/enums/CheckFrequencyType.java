package com.zkhf.epmis.platform.envManual.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum CheckFrequencyType {
    // 计划检测频次 1 日次、2 周次、3 月次、4 季度、5 半年、6 年、7 两年
    TYPE_R(1, "日次"),
    TYPE_Z(2, "周次"),
    TYPE_Y(3, "月次"),
    TYPE_JD(4, "季度"),
    TYPE_BN(5, "半年"),
    TYPE_N(6, "年"),
    TYPE_LN(7, "两年"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(CheckFrequencyType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    CheckFrequencyType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return "";
    }
}

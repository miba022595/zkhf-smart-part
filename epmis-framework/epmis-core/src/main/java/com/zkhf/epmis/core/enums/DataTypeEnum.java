package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DataTypeEnum {
    real(1, "实时数据"),
    minute(2, "分钟数据"),
    hour(3, "小时数据"),
    day(4, "日数据"),
    /* 下边的两个无实际数据意义 */
    week(5, "周"),
    month(6, "月"),
    year(7, "年"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(DataTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    DataTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return "";
    }

    public static String getCodeStr(Integer code) {
        if (real.code.equals(code)) {
            return "1,";
        } else if (minute.code.equals(code)) {
            return "2,";
        } else if (hour.code.equals(code)) {
            return "3,";
        } else if (day.code.equals(code)) {
            return "4,";
        }
        return "---";
    }
}

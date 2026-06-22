package com.zkhf.epmis.platform.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DeviceLifeUnitEnum {
    UNIT_MINUTE(2, "分钟"),
    UNIT_HOUR(3, "小时"),
    UNIT_DAY(4, "日"),
    UNIT_MONTH(5, "月"),
    UNIT_YEAR(6, "年"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(DeviceLifeUnitEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    DeviceLifeUnitEnum(Integer code, String name) {
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

package com.zkhf.epmis.process.alarm.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TackleTypeEnum {
    // 处理状态 0未处理；1处理过；其他先不管
    WCL(0, "未处理"),
    CLG(1, "处理过"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(TackleTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    TackleTypeEnum(Integer code, String name) {
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

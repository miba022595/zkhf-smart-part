package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum OutPutTypeEnum {
    OUT_PUT_FS(1, "废水"),
    OUT_PUT_FQ(2, "废气"),
    OUT_PUT_YC(3, "扬尘"),
    OUT_PUT_VOC(4, "VOC"),
    OUT_PUT_EC(5, "恶臭"),
    OUT_PUT_ZS(6, "噪声"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(OutPutTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    OutPutTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return "";
    }

    public static boolean notContainsType(Integer code) {
        return !containsType(code);
    }

    public static boolean containsType(Integer code) {
        if (null == code)
            return false;
        return CODE_TO_NAME.containsKey(code);
    }
}

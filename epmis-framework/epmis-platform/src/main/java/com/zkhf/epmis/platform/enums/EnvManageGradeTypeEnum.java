package com.zkhf.epmis.platform.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum EnvManageGradeTypeEnum {
    MANAGE_GRADE_BGS(1, "报告书"),
    MANAGE_GRADE_BGB(2, "报告表"),
    MANAGE_GRADE_DJB(3, "登记表"),
    MANAGE_GRADE_WXP(0, "无需环评"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(EnvManageGradeTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    EnvManageGradeTypeEnum(Integer code, String name) {
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

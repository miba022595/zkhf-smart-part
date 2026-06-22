package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ValidPeriodTypeEnum {
    P_W_X_K(1, "企业排污许可"),
    Q_T_Z_S(2, "企业其他证书"),
    J_C_R_W(3, "手工监测任务"),
    Y_J_W_Z(4, "应急物资"),
    ;

    public final Integer code;
    public final String desc;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(ValidPeriodTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.desc
                    ));

    ValidPeriodTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return "";
    }

    public static Map<Integer, String> getAll() {
        return CODE_TO_NAME;
    }
}

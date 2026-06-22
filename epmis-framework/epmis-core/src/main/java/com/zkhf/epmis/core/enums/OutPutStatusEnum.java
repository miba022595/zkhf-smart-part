package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum OutPutStatusEnum {
    OUT_PUT_STATUS_ZC(1, "正常"),
    OUT_PUT_STATUS_TY(2, "停运"),
    OUT_PUT_STATUS_GZ(3, "故障"),
    OUT_PUT_STATUS_LX(4, "离线"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(OutPutStatusEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    OutPutStatusEnum(Integer code, String name) {
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
        if (OUT_PUT_STATUS_ZC.code.equals(code)) {
            return "1,";
        } else if (OUT_PUT_STATUS_TY.code.equals(code)) {
            return "2,";
        } else if (OUT_PUT_STATUS_GZ.code.equals(code)) {
            return "3,";
        } else if (OUT_PUT_STATUS_LX.code.equals(code)) {
            return "4,";
        }
        return "---";
    }
}

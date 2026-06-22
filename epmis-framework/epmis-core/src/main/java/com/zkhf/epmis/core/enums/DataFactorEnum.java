package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DataFactorEnum {
    rtd("rtd", "实测值"),
    min("min", "最小值"),
    avg("avg", "平均值"),
    max("max", "最大值"),
    zsRtd("zsRtd", "折算实测值"),
    zsMin("zsMin", "折算最小值"),
    zsAvg("zsAvg", "折算平均值"),
    zsMax("zsMax", "折算最大值"),
    cou("cou", "累计排放量"),
    ;

    public final String code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String> CODE_TO_NAME =
            Arrays.stream(DataFactorEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    DataFactorEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        if (null != code && CODE_TO_NAME.containsKey(code)) {
            return CODE_TO_NAME.get(code);
        }
        return "";
    }
}

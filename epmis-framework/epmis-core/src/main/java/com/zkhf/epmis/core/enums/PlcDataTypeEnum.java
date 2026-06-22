package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据类型描述
 */
public enum PlcDataTypeEnum {
    BOOL(1, "bool"),
    INT16(2, "int16"),
    UINT16(3, "uint16"),
    INT32(4, "int32"),
    UINT32(5, "uint32"),
    FLOAT32(6, "float32"),
    FLOAT64(7, "float64"),
    ;

    public final Integer type;
    public final String desc;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> enumMap =
            Arrays.stream(PlcDataTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.type,
                            e -> e.desc
                    ));

    PlcDataTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static String getDescByType(Integer type) {
        if (null != type && enumMap.containsKey(type)) {
            return enumMap.get(type);
        }
        return "未知";
    }

    public static boolean containsType(Integer type) {
        return null != type && enumMap.containsKey(type);
    }
}

package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点位类型描述
 */
public enum PlcPointTypeEnum {
    DI(1, "DI数字输入"),
    DO(2, "DO数字输出"),
    AI(3, "AI模拟输入"),
    AO(4, "AO模拟输出"),
    REGISTER(5, "寄存器"),
    ;

    public final Integer type;
    public final String desc;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> enumMap =
            Arrays.stream(PlcPointTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.type,
                            e -> e.desc
                    ));

    PlcPointTypeEnum(Integer type, String desc) {
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

    /**
     * 将数据库中的Modbus地址转换为协议中使用的地址（减去基数）
     * @param type 点位类型（1:DI, 2:DO, 3:AI, 4:AO）
     * @return 协议中使用的地址（如 0, 1, 2...）
     */
    public static int getCardinalNumber(Integer type) {
        if (PlcPointTypeEnum.DI.type.equals(type)) {
            // DI - 数字量输入 (1x区，10001-19999)
            return 10000;
        } else if (PlcPointTypeEnum.DO.type.equals(type)) {
            // DO - 数字量输出 (0x区，00001-09999)
            return 0;
        } else if (PlcPointTypeEnum.AI.type.equals(type)) {
            // AI - 模拟量输入 (3x区，30001-39999)
            return 30000;
        } else if (PlcPointTypeEnum.AO.type.equals(type)) {
            // AO - 模拟量输出 (4x区，40001-49999)
            return 40000;
        }
        return 0;
    }
}

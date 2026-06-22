package com.zkhf.epmis.protocol.common.enums;

/**
 * 时间类型枚举
 */
public enum DATAEnum {
    //TID 实时数据报文类型
    REAL("2011", "real"),

    //TID 日数据报文类型
    DAY("2031", "day"),
    //TID 小时数据报文类型
    HOUR("2061", "hour"),
    //TID 分钟数据报文类型
    MIN("2051", "minute"),
    // 废气标识
    FEI_QI("31","ST"),
    // 废水标识
    FEI_SHUI("32","ST"),
    // 无组织标标识
    WU_ZU_ZHI("22","ST");

    public final String code;
    public final String desc;

    DATAEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getEnumByCode(String code) {
        for (DATAEnum e : DATAEnum.values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        return null;
    }
}

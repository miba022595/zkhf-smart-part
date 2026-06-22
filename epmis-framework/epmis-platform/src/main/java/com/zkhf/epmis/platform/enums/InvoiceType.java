package com.zkhf.epmis.platform.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum InvoiceType {
    VAT_SPECIAL("VAT_SPECIAL", "增值税专用发票"),
    VAT_NORMAL("VAT_NORMAL","增值税普通发票"),
    VAT_ELECTRONIC("VAT_ELECTRONIC","增值税电子普通发票"),
    VAT_ROLL("VAT_ROLL","增值税普通发票(卷式)"),
    TRANSPORT("TRANSPORT","运输业专用发票"),
    CONSTRUCTION("CONSTRUCTION","建筑业统一发票"),
    VEHICLE_SALE("VEHICLE_SALE", "机动车销售统一发票"),
    GENERAL("GENERAL","通用机打发票"),
    QUOTA("QUOTA","定额发票"),
    ;

    public final String code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String> CODE_TO_NAME =
            Arrays.stream(InvoiceType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    InvoiceType(String code, String name) {
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

package com.zkhf.epmis.platform.approval.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ApprovalInstanceStatus {
    PROCESSING("PROCESSING", "审批中"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已取消"),
    ;

    public final String code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String> CODE_TO_NAME =
            Arrays.stream(ApprovalInstanceStatus.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    ApprovalInstanceStatus(String code, String name) {
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

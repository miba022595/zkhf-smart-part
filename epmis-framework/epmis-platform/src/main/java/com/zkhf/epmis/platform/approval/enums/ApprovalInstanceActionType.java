package com.zkhf.epmis.platform.approval.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ApprovalInstanceActionType {
    START("START", "发起审批"),
    APPROVE("APPROVE", "同意"),
    REJECT("REJECT", "拒绝"),
    CANCEL("CANCEL", "撤回"),
    ;

    public final String code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String> CODE_TO_NAME =
            Arrays.stream(ApprovalInstanceActionType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    ApprovalInstanceActionType(String code, String name) {
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

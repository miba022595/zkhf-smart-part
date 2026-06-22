package com.zkhf.epmis.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum GenApprovalType {
    // 审批状态：1-草稿、2-已提交、3-审核中、4-审核通过、5-审核拒绝、6已取消
    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    REVIEWING(3, "审核中"),
    APPROVED(4, "审核通过"),
    REJECTED(5, "审核拒绝"),
    CANCELLED(6, "已取消");
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(GenApprovalType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    GenApprovalType(Integer code, String name) {
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

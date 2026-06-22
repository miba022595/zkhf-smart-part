package com.zkhf.epmis.platform.ops.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum OpsTaskType {
    // 运维任务状态：1-草稿、2-已下发、3-已接收、4-已取消
    DRAFT(1, "草稿"),
    ISSUED(2, "已下发"),
    RECEIVED(3, "已接收"),
    CANCELLED(4, "已取消"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(OpsTaskType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    OpsTaskType(Integer code, String name) {
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

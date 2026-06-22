package com.zkhf.epmis.platform.envManual.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TaskStatusType {
    // 任务状态：0-已取消，1-待下发，2-待执行，3-已完成
    STATUS_YQX(0, "已取消"),
    STATUS_DXF(1, "待下发"),
    STATUS_DZX(2, "待执行"),
    STATUS_YWC(3, "已完成"),
    STATUS_SPZ(4, "审批中"),
    STATUS_YJJ(5, "已拒绝"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(TaskStatusType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    TaskStatusType(Integer code, String name) {
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

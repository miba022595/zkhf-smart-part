package com.zkhf.epmis.platform.envManual.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PlanStatusType {
    // 计划状态：0-已取消、1-草稿、2-待审批、3-已审批、4-已完成、5-已终止
    STATUS_YQX(0, "已取消"),
    STATUS_CG(1, "草稿"),
    STATUS_DSP(2, "待审批"),
    STATUS_YSP(3, "已审批"),
    STATUS_YWC(4, "已完成"),
    STATUS_YZZ(5, "已终止"),
    STATUS_SPZ(6, "审批中"),
    STATUS_YJJ(7, "已拒绝"),
    ;

    public final Integer code;
    public final String name;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> CODE_TO_NAME =
            Arrays.stream(PlanStatusType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.name
                    ));

    PlanStatusType(Integer code, String name) {
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

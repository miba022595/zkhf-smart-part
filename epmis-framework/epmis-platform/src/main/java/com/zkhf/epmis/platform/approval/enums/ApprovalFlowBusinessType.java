package com.zkhf.epmis.platform.approval.enums;

import com.zkhf.epmis.core.constant.Constants;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** 供后端使用 */
public enum ApprovalFlowBusinessType {
    envManualCheckPlan(Constants.APPROVAL_FLOW_ENV_MANUAL_CHECK_PLAN, "envManualCheckPlanService"), // 手工检测执行计划
    envManualCheckTask(Constants.APPROVAL_FLOW_ENV_MANUAL_CHECK_TASK, "envManualCheckTaskService"), // 手工检测执行任务
    opsRecord(Constants.APPROVAL_FLOW_OPS_RECORD, "opsRecordService"), // 运维记录
    materialApply(Constants.APPROVAL_FLOW_MATERIAL_APPLY, "materialApprovalMqttService"),
    materialIn(Constants.APPROVAL_FLOW_MATERIAL_IN, "materialApprovalMqttService"),
    materialOut(Constants.APPROVAL_FLOW_MATERIAL_OUT, "materialApprovalMqttService"),
    materialReturn(Constants.APPROVAL_FLOW_MATERIAL_RETURN, "materialApprovalMqttService"),
    ;

    public final String type;
    public final String beanName;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String> TYPE_TO_BEAN =
            Arrays.stream(ApprovalFlowBusinessType.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.type,
                            e -> e.beanName
                    ));

    ApprovalFlowBusinessType(String type, String beanName) {
        this.type = type;
        this.beanName = beanName;
    }

    public static String getNameByCode(String type) {
        if (null != type && TYPE_TO_BEAN.containsKey(type)) {
            return TYPE_TO_BEAN.get(type);
        }
        return "";
    }
}

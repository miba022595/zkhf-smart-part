package com.zkhf.epmis.platform.task.handler;

import com.github.f4b6a3.ulid.UlidCreator;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.envManual.domain.EnvManualInitTask;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import com.zkhf.epmis.platform.envManual.enums.PlanStatusType;
import com.zkhf.epmis.platform.envManual.enums.TaskStatusType;
import com.zkhf.epmis.platform.mapper.task.TaskHandlerMapper;
import com.zkhf.epmis.platform.task.domain.TaskPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * xxljob自动任务
 */
@Component
public class TaskJobHandler {

    private TaskHandlerMapper taskHandlerMapper;
    @Autowired
    public void setTaskHandlerMapper(TaskHandlerMapper taskHandlerMapper) {
        this.taskHandlerMapper = taskHandlerMapper;
    }

    /**
     * 环境手工检测计划生成任务
     * 获取已审批状态下的计划列表，执行计划状态变更后需要自动生成的任务
     * 状态变更时生成间隔一周内的数据，自动任务第二天凌晨执行，时间点也应该是+6天
     */
    @XxlJob("envManualCheckPlanCreateTask")
    public void envManualCheckPlanCreateTask() {
        // 获取6天后的时间，进行时间命中校验
        LocalDate hitDate = LocalDate.now().plusDays(6);
        List<EnvManualInitTask> taskList = new ArrayList<>();
        // 获取已审批的计划列表
        List<TaskPlan> planList = taskHandlerMapper.selectPlanList(PlanStatusType.STATUS_YSP.code);
        if (planList != null && !planList.isEmpty()) {
            for (TaskPlan plan : planList) {
                if (null == plan.getFirstDate()) {
                    continue;
                }
                // 首次执行时间在限定时间之后的跳过
                if (plan.getFirstDate().isAfter(hitDate)) {
                    continue;
                }
                if (StringUtils.isEmpty(plan.getCheckFrequency())) {
                    continue;
                }
                for (String freq : plan.getCheckFrequency().split(",")) {
                    int code = StringUtils.strToInt(freq, 0);
                    if (CheckFrequencyType.TYPE_R.code.equals(code)) { // 日次
                        addTask(taskList, plan.getOutPutPollId(), hitDate, code);
                    } else if (CheckFrequencyType.TYPE_Z.code.equals(code)) { // 周次-判断相差整周的倍数
                        long days = ChronoUnit.DAYS.between(plan.getFirstDate(), hitDate);
                        if (days % 7 == 0) {
                            addTask(taskList, plan.getOutPutPollId(), hitDate, code);
                        }
                    } else if (CheckFrequencyType.TYPE_Y.code.equals(code)) { // 月次-判断相差整月的倍数
                        long months = ChronoUnit.MONTHS.between(plan.getFirstDate(), hitDate);
                        if (plan.getFirstDate().plusMonths(months).equals(hitDate)) {
                            addTask(taskList, plan.getOutPutPollId(), hitDate, code);
                        }
                    } else if (CheckFrequencyType.TYPE_JD.code.equals(code)) { // 季度-判断相差整3个月的倍数
                        long months = ChronoUnit.MONTHS.between(plan.getFirstDate(), hitDate);
                        if (months % 3 == 0 && plan.getFirstDate().plusMonths(months).equals(hitDate)) {
                            addTask(taskList, plan.getOutPutPollId(), hitDate, code);
                        }
                    } else if (CheckFrequencyType.TYPE_BN.code.equals(code)) { // 半年-判断相差整6个月的倍数
                        long months = ChronoUnit.MONTHS.between(plan.getFirstDate(), hitDate);
                        if (months % 6 == 0 && plan.getFirstDate().plusMonths(months).equals(hitDate)) {
                            addTask(taskList, plan.getOutPutPollId(), hitDate, code);
                        }
                    } else if (CheckFrequencyType.TYPE_N.code.equals(code)) { // 年-判断相差整年的倍数
                        long years = ChronoUnit.YEARS.between(plan.getFirstDate(), hitDate);
                        if (plan.getFirstDate().plusYears(years).equals(hitDate)) {
                            addTask(taskList, plan.getOutPutPollId(), hitDate, CheckFrequencyType.TYPE_N.code);
                        }
                    } else if (CheckFrequencyType.TYPE_LN.code.equals(code)) { // 两年一次-判断相差2年的倍数
                        long years = ChronoUnit.YEARS.between(plan.getFirstDate(), hitDate);
                        if (years % 2 == 0 && plan.getFirstDate().plusYears(years).equals(hitDate)) {
                            addTask(taskList, plan.getOutPutPollId(), hitDate, code);
                        }
                    }
                }
            }
        }
        if (!taskList.isEmpty()) {
            // 插入预警信息
            taskHandlerMapper.batchInsertEnvManualCheckTask(taskList);
        }
    }

    private void addTask(List<EnvManualInitTask> taskList, String outPutPollId, LocalDate taskDate, Integer checkFrequency) {
        taskList.add(EnvManualInitTask.builder()
                .taskId(UlidCreator.getMonotonicUlid().toString())
                .outPutPollId(outPutPollId)
                .taskDate(taskDate)
                .status(TaskStatusType.STATUS_DXF.code)
                .checkFrequency(checkFrequency)
                .build());
    }
}

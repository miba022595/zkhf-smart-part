package com.zkhf.epmis.platform.task.handler;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import com.zkhf.epmis.platform.mapper.task.TaskHandlerMapper;
import com.zkhf.epmis.platform.ops.domain.OpsTask;
import com.zkhf.epmis.platform.ops.domain.OpsTaskConf;
import com.zkhf.epmis.platform.ops.enums.OpsTaskType;
import com.zkhf.epmis.platform.ops.service.OpsRecordService;
import com.zkhf.epmis.platform.ops.service.OpsTaskService;
import com.zkhf.epmis.platform.task.domain.OpsTaskStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xxljob自动任务-运维任务
 *
 */
@Component
public class OpsTaskJobHandler {

    private TaskHandlerMapper taskHandlerMapper;
    @Autowired
    public void setTaskHandlerMapper(TaskHandlerMapper taskHandlerMapper) {
        this.taskHandlerMapper = taskHandlerMapper;
    }

    /**
     * 运维任务的自动任务
     * 负责
     *  1. 每天的自动运维任务生成
     *  2. 运维任务状况统计
     * 每天凌晨执行
     */
    @XxlJob("opsTaskTask")
    public void opsTaskTask() {
        // 自动运维任务生成
        opsTaskAutoCreateTask();
        // 运维任务情况统计
        opsTaskStatTask();
    }

    /**
     * 依据任务配置自动生成运维任务
     */
    private void opsTaskAutoCreateTask() {
        // 获取启动下，且开始时间不为空的运维任务配置信息
        List<OpsTaskConf> confList = taskHandlerMapper.selectOpsTaskConfList();
        if (null == confList || confList.isEmpty()) {
            return;
        }
        // 获取6天后的时间，进行时间命中校验
        LocalDate hitDate = LocalDate.now().plusDays(6);
        List<OpsTask> taskList = new ArrayList<>();
        for (OpsTaskConf conf : confList) {
            if (null == conf.getBeginDate()) {
                continue;
            }
            // 任务开始时间在限定时间之后的跳过
            if (conf.getBeginDate().isAfter(hitDate)) {
                continue;
            }
            Integer cycleType = conf.getCycleType(), cycleValue = conf.getCycleValue();
            if (null == cycleType || null == cycleValue || cycleValue < 1) {
                continue;
            }
            // cycleType 执行周期类型（1：日，2：周，3：月，4：季度，6：年） cycleValue 周期数值（如：每3天，每2周等）
            // 如 cycleType：2，cycleValue：3，标识每3周一次
            if (CheckFrequencyType.TYPE_R.code.equals(cycleType)) { // 日次
                long days = ChronoUnit.DAYS.between(conf.getBeginDate(), hitDate);
                if (days % cycleValue == 0) {
                    addTask(taskList, conf, hitDate);
                }
            } else if (CheckFrequencyType.TYPE_Z.code.equals(cycleType)) { // 周次-判断相差整周的倍数
                long days = ChronoUnit.DAYS.between(conf.getBeginDate(), hitDate);
                if (days % (7L * cycleValue) == 0) {
                    addTask(taskList, conf, hitDate);
                }
            } else if (CheckFrequencyType.TYPE_Y.code.equals(cycleType)) { // 月次-判断相差整月的倍数
                long months = ChronoUnit.MONTHS.between(conf.getBeginDate(), hitDate) * cycleValue;
                if (conf.getBeginDate().plusMonths(months).equals(hitDate)) {
                    addTask(taskList, conf, hitDate);
                }
            } else if (CheckFrequencyType.TYPE_JD.code.equals(cycleType)) { // 季度-判断相差整3个月的倍数
                long months = ChronoUnit.MONTHS.between(conf.getBeginDate(), hitDate);
                if (months % 3 == 0 && conf.getBeginDate().plusMonths(months * cycleValue).equals(hitDate)) {
                    addTask(taskList, conf, hitDate);
                }
            } else if (CheckFrequencyType.TYPE_N.code.equals(cycleType)) { // 年-判断相差整年的倍数
                long years = ChronoUnit.YEARS.between(conf.getBeginDate(), hitDate);
                if (conf.getBeginDate().plusYears(years * cycleValue).equals(hitDate)) {
                    addTask(taskList, conf, hitDate);
                }
            }
        }
        if (!taskList.isEmpty()) {
            // 插入运维任务信息
            taskHandlerMapper.batchInsertOpsTaskList(taskList);
        }
    }

    private void addTask(List<OpsTask> taskList, OpsTaskConf conf, LocalDate hitDate) {
        OpsTask task = new OpsTask();
        task.setTaskId(UlidCreator.getMonotonicUlid().toString());
        task.setEntCode(conf.getEntCode());
        task.setOutPutId(conf.getOutPutId());
        task.setTemplateCode(conf.getTemplateCode());
        task.setTaskType(OpsTaskService.taskAuto);
        task.setTaskStatus(OpsTaskType.DRAFT.code);
        task.setPlanDate(hitDate);
        task.setEarlyDays(conf.getEarlyDays());
        task.setCreateId(conf.getCreateId());
        taskList.add(task);
    }

    /**
     * 运维任务情况统计
     */
    private void opsTaskStatTask() {
        // 获取审批通过的运维记录列表
        List<Map<String, Object>> recordList = taskHandlerMapper.selectOpsRecordList();
        // 获取运维任务列表
        List<Map<String, Object>> taskList = taskHandlerMapper.selectOpsTaskList();
        Map<String, OpsTaskStat> statMap = new HashMap<>();
        OpsTaskStat stat;
        if (null != recordList) {
            for (Map<String, Object> record : recordList) {
                String ent_code = MapUtil.getStr(record, "ent_code") ,
                        out_put_id = MapUtil.getStr(record, "out_put_id"),
                        template_code = MapUtil.getStr(record, "template_code"),
                        record_date = MapUtil.getStr(record, "record_date");
                if (StringUtils.isEmpty(ent_code) || StringUtils.isEmpty(out_put_id) ||
                        StringUtils.isEmpty(template_code) || StringUtils.isEmpty(record_date)) {
                    continue;
                }
                Long ops_user_id = MapUtil.getLong(record, "ops_user_id");
                if (null == ops_user_id) {
                    continue;
                }
                stat = getOpsTaskStat(statMap, ent_code, out_put_id, template_code, ops_user_id, record_date);
                Integer qualified_flag = MapUtil.getInt(record, "qualified_flag");
                if (OpsRecordService.qualifiedS.equals(qualified_flag)) {
                    if (null == stat.getQualifiedTasks()) {
                        stat.setQualifiedTasks(1);
                    } else {
                        stat.setQualifiedTasks(stat.getQualifiedTasks() + 1);
                    }
                } else if (OpsRecordService.qualifiedE.equals(qualified_flag)) {
                    if (null == stat.getUnqualifiedTasks()) {
                        stat.setUnqualifiedTasks(1);
                    } else {
                        stat.setUnqualifiedTasks(stat.getUnqualifiedTasks() + 1);
                    }
                }
                if (null == stat.getCompletedTasks()) {
                    stat.setCompletedTasks(1);
                } else {
                    stat.setCompletedTasks(stat.getCompletedTasks() + 1);
                }
            }
        }
        if (null != taskList) {
            for (Map<String, Object> task : taskList) {
                String ent_code = MapUtil.getStr(task, "ent_code") ,
                        out_put_id = MapUtil.getStr(task, "out_put_id"),
                        template_code = MapUtil.getStr(task, "template_code"),
                        plan_date = MapUtil.getStr(task, "plan_date");
                if (StringUtils.isEmpty(ent_code) || StringUtils.isEmpty(out_put_id) ||
                        StringUtils.isEmpty(template_code) || StringUtils.isEmpty(plan_date)) {
                    continue;
                }
                Long operator = MapUtil.getLong(task, "operator");
                if (null == operator) {
                    continue;
                }
                stat = getOpsTaskStat(statMap, ent_code, out_put_id, template_code, operator, plan_date);
                Integer task_type = MapUtil.getInt(task, "task_type");
                if (OpsTaskService.taskAuto.equals(task_type)) {
                    if (null == stat.getAutoTasks()) {
                        stat.setAutoTasks(1);
                    } else {
                        stat.setAutoTasks(stat.getAutoTasks() + 1);
                    }
                } else if (OpsTaskService.taskManual.equals(task_type)) {
                    if (null == stat.getManualTasks()) {
                        stat.setManualTasks(1);
                    } else {
                        stat.setManualTasks(stat.getManualTasks() + 1);
                    }
                }
            }
        }
        taskHandlerMapper.truncateOpsTaskStat();
        if (!statMap.isEmpty()) {
            // 插入运维任务信息
            taskHandlerMapper.batchInsertOpsStatList(new ArrayList<>(statMap.values()));
        }
    }

    private OpsTaskStat getOpsTaskStat(Map<String, OpsTaskStat> statMap, String ent_code, String out_put_id, String template_code, Long operator, String taskStatDate) {
        String key = ent_code + "_" + out_put_id + "_" + template_code + "_" + operator + "_" + taskStatDate;
        if (statMap.containsKey(key)) {
            return statMap.get(key);
        }
        OpsTaskStat stat = new OpsTaskStat();
        stat.setStatId(UlidCreator.getMonotonicUlid().toString());
        stat.setEntCode(ent_code);
        stat.setOutPutId(out_put_id);
        stat.setTemplateCode(template_code);
        stat.setOperator(operator);
        stat.setTaskStatDate(LocalDate.parse(taskStatDate, DateTimeFormatter.ISO_DATE));
        statMap.put(key, stat);
        return stat;
    }
}

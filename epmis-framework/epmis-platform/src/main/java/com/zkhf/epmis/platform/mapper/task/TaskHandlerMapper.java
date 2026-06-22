package com.zkhf.epmis.platform.mapper.task;

import com.zkhf.epmis.platform.envManual.domain.EnvManualInitTask;
import com.zkhf.epmis.platform.ops.domain.OpsTask;
import com.zkhf.epmis.platform.ops.domain.OpsTaskConf;
import com.zkhf.epmis.platform.task.domain.OpsTaskStat;
import com.zkhf.epmis.platform.task.domain.TaskPlan;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface TaskHandlerMapper {

    /**
     * 环境手工检测计划表
     */
    @Select("select out_put_poll_id as outPutPollId, first_date as firstDate, check_frequency as checkFrequency from t_env_manual_check_plan where status = #{status} ")
    List<TaskPlan> selectPlanList(@Param("status") Integer status);

    /**
     * 批量新增环境手工检测任务
     */
    @Insert("<script> " +
            " insert into t_env_manual_check_task " +
            "   (task_id, out_put_poll_id, task_date, check_frequency) " +
            " values " +
            "   <foreach item='task' collection='taskList' separator=','> " +
            "       (#{task.taskId}, #{task.outPutPollId}, #{task.taskDate}, #{task.checkFrequency}) " +
            "   </foreach> " +
            "</script> ")
    void batchInsertEnvManualCheckTask(@Param("taskList") List<EnvManualInitTask> taskList);

    /**
     * 获取启动的运维任务配置信息
     */
    @Select(" select ent_code as entCode, out_put_id as outPutId, template_code as templateCode, begin_date as beginDate, cycle_type as cycleType, cycle_value as cycleValue, early_days as earlyDays " +
            " from t_ops_task_conf where enabled = 1 and begin_date is not null ")
    List<OpsTaskConf> selectOpsTaskConfList();

    /**
     * 批量新增运维自动任务
     */
    @Insert("<script> " +
            " insert into t_ops_task " +
            "   (task_id, ent_code, out_put_id, template_code, task_type, task_status, plan_date, early_days, create_id, create_time) " +
            " values " +
            "   <foreach item='task' collection='taskList' separator=','> " +
            "       (#{task.taskId}, #{task.entCode}, #{task.outPutId}, #{task.templateCode}, #{task.taskType}, #{task.taskStatus}, #{task.planDate}, #{task.earlyDays}, " +
            "       #{task.createId}, now()) " +
            "   </foreach> " +
            "</script> ")
    void batchInsertOpsTaskList(@Param("taskList") List<OpsTask> taskList);

    /**
     * 获取审批通过的运维记录列表
     */
    @Select(" select ent_code, out_put_id, template_code, IFNULL(DATE_FORMAT(record_date, '%Y-%m-%d'), '') as record_date, ops_user_id, qualified_flag " +
            " from t_ops_record where review_status = 4 ")
    List<Map<String, Object>> selectOpsRecordList();

    /**
     * 获取运维任务列表
     */
    @Select(" select ent_code, out_put_id, template_code, IFNULL(DATE_FORMAT(plan_date, '%Y-%m-%d'), '') as plan_date, operator, task_type " +
            " from t_ops_task ")
    List<Map<String, Object>> selectOpsTaskList();

    /**
     * 清空运维任务统计表
     */
    @Update("truncate table t_ops_task_stat")
    void truncateOpsTaskStat();

    /**
     * 批量新增运维任务统计信息
     */
    @Insert("<script> " +
            " insert into t_ops_task_stat " +
            "   (stat_id, ent_code, out_put_id, template_code, operator, task_stat_date, auto_tasks, manual_tasks, completed_tasks, qualified_tasks, unqualified_tasks, create_time) " +
            " values " +
            "   <foreach item='stat' collection='statList' separator=','> " +
            "       (#{stat.statId}, #{stat.entCode}, #{stat.outPutId}, #{stat.templateCode}, #{stat.operator}, #{stat.taskStatDate}, #{stat.autoTasks}, #{stat.manualTasks}, " +
            "       #{stat.completedTasks}, #{stat.qualifiedTasks}, #{stat.unqualifiedTasks}, now()) " +
            "   </foreach> " +
            "</script> ")
    void batchInsertOpsStatList(@Param("statList") List<OpsTaskStat> statList);
}
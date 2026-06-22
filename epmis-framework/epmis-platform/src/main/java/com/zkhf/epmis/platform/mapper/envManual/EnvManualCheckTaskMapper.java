package com.zkhf.epmis.platform.mapper.envManual;

import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTaskReq;
import com.zkhf.epmis.platform.envManual.domain.EnvManualInitTask;

import java.util.List;

/**
 * 环境手工检测任务Mapper接口
 */
public interface EnvManualCheckTaskMapper {

    /**
     * 查询环境手工检测任务
     */
    EnvManualCheckTask selectEnvManualCheckByTaskId(String taskId);

    /**
     * 批量新增环境手工检测任务
     */
    void batchInsertEnvManualCheckTask(List<EnvManualInitTask> taskList);

    /**
     * 查询环境手工检测任务列表
     */
    List<EnvManualCheckTask> selectEnvManualCheckTaskList(EnvManualCheckTaskReq req);

    /**
     * 修改环境手工检测任务
     */
    int updateEnvManualCheckTask(EnvManualCheckTask info);

    /**
     * 修改环境手工检测任务-导入修改
     */
    int batchUpdateEnvManualCheckTask(List<EnvManualCheckTask> taskList);

}

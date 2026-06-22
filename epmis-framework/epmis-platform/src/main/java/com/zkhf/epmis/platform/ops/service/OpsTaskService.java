package com.zkhf.epmis.platform.ops.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsTask;
import com.zkhf.epmis.platform.ops.domain.OpsTaskConf;
import com.zkhf.epmis.platform.ops.domain.OpsTaskReq;

/**
 * 运维任务Service接口
 */
public interface OpsTaskService {

    /** 是否启动任务（1：启动，0：不启动） */
    Integer taskEnable = 1;
    /** 是否启动任务（1：启动，0：不启动） */
    Integer taskUnable = 0;
    /** 任务类型（1：自动生成的任务，2：手动生成的任务） */
    Integer taskAuto = 1;
    /** 任务类型（1：自动生成的任务，2：手动生成的任务） */
    Integer taskManual = 2;

    /**
     * 查询运维任务配置
     */
    AjaxResult selectOpsTaskConfDetail(String entCode, String outPutId, String templateCode);

    /**
     * 查询运维任务配置列表
     */
    AjaxResult selectOpsTaskConfList(OpsTaskReq req);

    /**
     * 新增运维任务配置
     */
    AjaxResult insertOpsTaskConf(OpsTaskConf info);

    /**
     * 修改运维任务配置
     */
    AjaxResult updateOpsTaskConf(OpsTaskConf info);

    /**
     * 删除运维任务配置信息
     */
    AjaxResult deleteOpsTaskConf(String entCode, String outPutId, String templateCode);

    /**
     * 查询运维任务
     */
    AjaxResult selectOpsTaskByTaskId(String taskId);

    /**
     * 查询运维个人的任务列表（已下发、已接受状态）
     */
    AjaxResult selectPersonOpsTaskList(OpsTaskReq req);

    /**
     * 查询运维任务列表
     */
    AjaxResult selectOpsTaskList(OpsTaskReq req);

    /**
     * 新增运维任务
     */
    AjaxResult insertOpsTask(OpsTask info);

    /**
     * 修改运维任务
     */
    AjaxResult updateOpsTask(OpsTask info);

    /**
     * 删除运维任务信息
     */
    AjaxResult deleteOpsTaskByTaskId(String taskId);
}

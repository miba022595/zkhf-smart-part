package com.zkhf.epmis.platform.ops.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsTask;
import com.zkhf.epmis.platform.ops.domain.OpsTaskConf;
import com.zkhf.epmis.platform.ops.domain.OpsTaskReq;
import com.zkhf.epmis.platform.ops.service.OpsTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 运维任务Controller
 */
@RestController
@RequestMapping("/platform/ops/task")
public class OpsTaskController {

    private OpsTaskService opsTaskService;
    @Autowired
    public void setOpsTaskService(OpsTaskService opsTaskService) {
        this.opsTaskService = opsTaskService;
    }

    /**
     * 获取运维任务配置详细信息
     */
    @GetMapping(value = "/conf/getInfo")
    public AjaxResult getConfInfo(@RequestParam(required = false) String entCode,
                              @RequestParam(required = false) String outPutId,
                              @RequestParam(required = false) String templateCode) {
        return opsTaskService.selectOpsTaskConfDetail(entCode, outPutId, templateCode);
    }

    /**
     * 查询运维任务配置列表
     */
    @PostMapping("/conf/list")
    public AjaxResult confList(@RequestBody(required = false) OpsTaskReq req) {
        return opsTaskService.selectOpsTaskConfList(req);
    }

    /**
     * 新增运维任务配置
     */
    @PostMapping("/conf")
    public AjaxResult addConf(@RequestBody OpsTaskConf info) {
        return opsTaskService.insertOpsTaskConf(info);
    }

    /**
     * 修改运维任务配置
     */
    @PutMapping("/conf")
    public AjaxResult editConf(@RequestBody OpsTaskConf info) {
        return opsTaskService.updateOpsTaskConf(info);
    }

    /**
     * 删除运维任务配置
     */
    @DeleteMapping("/conf")
    public AjaxResult removeConf(@RequestParam(required = false) String entCode,
                                 @RequestParam(required = false) String outPutId,
                                 @RequestParam(required = false) String templateCode) {
        return opsTaskService.deleteOpsTaskConf(entCode, outPutId, templateCode);
    }

    /**
     * 获取运维任务详细信息
     */
    @GetMapping(value = "/task/{taskId}")
    public AjaxResult getTaskInfo(@PathVariable("taskId") String taskId) {
        return opsTaskService.selectOpsTaskByTaskId(taskId);
    }

    /**
     * 查询运维个人的任务列表（已下发、已接受状态）
     */
    @PostMapping("/task/personList")
    public AjaxResult taskPersonList(@RequestBody(required = false) OpsTaskReq req) {
        return opsTaskService.selectPersonOpsTaskList(req);
    }

    /**
     * 查询运维任务列表
     */
    @PostMapping("/task/list")
    public AjaxResult taskList(@RequestBody(required = false) OpsTaskReq req) {
        return opsTaskService.selectOpsTaskList(req);
    }

    /**
     * 新增运维任务
     */
    @PostMapping("/task")
    public AjaxResult addTask(@RequestBody OpsTask info) {
        return opsTaskService.insertOpsTask(info);
    }

    /**
     * 修改运维任务
     */
    @PutMapping("/task")
    public AjaxResult editTask(@RequestBody OpsTask info) {
        return opsTaskService.updateOpsTask(info);
    }

    /**
     * 删除运维任务
     */
    @DeleteMapping("/task/{taskId}")
    public AjaxResult removeTask(@PathVariable String taskId) {
        return opsTaskService.deleteOpsTaskByTaskId(taskId);
    }
}

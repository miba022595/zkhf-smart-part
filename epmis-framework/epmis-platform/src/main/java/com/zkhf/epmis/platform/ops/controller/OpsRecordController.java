package com.zkhf.epmis.platform.ops.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsRecord;
import com.zkhf.epmis.platform.ops.domain.OpsRecordReq;
import com.zkhf.epmis.platform.ops.service.OpsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 运维记录Controller
 */
@RestController
@RequestMapping("/platform/ops/record")
public class OpsRecordController {

    private OpsRecordService opsRecordService;
    @Autowired
    public void setOpsRecordService(OpsRecordService opsRecordService) {
        this.opsRecordService = opsRecordService;
    }

    /**
     * 获取运维记录详细信息
     */
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") String recordId) {
        return opsRecordService.selectOpsRecordById(recordId);
    }

    /**
     * 查询运维记录列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) OpsRecordReq req) {
        return opsRecordService.selectOpsRecordList(req);
    }

    /**
     * 新增运维记录
     */
    @PostMapping
    public AjaxResult add(@RequestBody OpsRecord info) {
        return opsRecordService.insertOpsRecord(info);
    }

    /**
     * 修改运维记录
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OpsRecord info) {
        return opsRecordService.updateOpsRecord(info);
    }

    /**
     * 删除运维记录
     */
    @DeleteMapping("/{recordId}")
    public AjaxResult remove(@PathVariable String recordId) {
        return opsRecordService.deleteOpsRecordById(recordId);
    }

    /**
     * 查询运维任务统计
     */
    @PostMapping("/task/stat")
    public AjaxResult taskStat(@RequestBody(required = false) OpsRecordReq req) {
        return opsRecordService.selectOpsTaskStat(req);
    }
}

package com.zkhf.epmis.platform.ops.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecord;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecordReq;
import com.zkhf.epmis.platform.ops.domain.OpsClock;
import com.zkhf.epmis.platform.ops.service.OpsAttRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 考勤打卡记录Controller
 */
@RestController
@RequestMapping("/platform/ops/attRecord")
public class OpsAttRecordController {

    private OpsAttRecordService opsAttRecordService;
    @Autowired
    public void setOpsAttRecordService(OpsAttRecordService opsAttRecordService) {
        this.opsAttRecordService = opsAttRecordService;
    }

    /**
     * 查询考勤打卡记录列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) OpsAttRecordReq req) {
        return opsAttRecordService.selectOpsAttRecordList(req);
    }

    /**
     * 导出考勤打卡记录列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) OpsAttRecordReq req, HttpServletResponse response) {
        opsAttRecordService.exportOpsAttRecord(req, response);
    }

    /**
     * 获取最新一次的打卡记录，用于判断是签到还是签退
     */
    @PostMapping(value = "/newestRecord")
    public AjaxResult selectNewestRecord(@RequestBody(required = false) OpsAttRecordReq req) {
        return opsAttRecordService.selectNewestOpsAttRecord(req);
    }

    /**
     * 考勤打卡
     */
    @PostMapping("clock")
    public AjaxResult clock(@RequestBody OpsClock info) {
        return opsAttRecordService.opsAttClock(info);
    }

    /**
     * 考勤协助
     */
    @PutMapping("assistant")
    public AjaxResult assistant(@RequestBody OpsAttRecord info) {
        return opsAttRecordService.opsAttAssistant(info);
    }

    /**
     * 删除考勤打卡记录
     */
    @DeleteMapping("/{recordId}")
    public AjaxResult remove(@PathVariable String recordId) {
        return opsAttRecordService.deleteOpsAttRecordById(recordId);
    }
}

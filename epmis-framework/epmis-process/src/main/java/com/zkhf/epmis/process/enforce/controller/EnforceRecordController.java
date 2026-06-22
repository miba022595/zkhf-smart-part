package com.zkhf.epmis.process.enforce.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.enforce.domain.EnforceRecord;
import com.zkhf.epmis.process.enforce.domain.EnforceRecordReq;
import com.zkhf.epmis.process.enforce.service.EnforceRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 执法检查记录Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/enforceRecord")
public class EnforceRecordController {

    private EnforceRecordService enforceRecordService;
    @Autowired
    public void setEnforceRecordService(EnforceRecordService enforceRecordService) {
        this.enforceRecordService = enforceRecordService;
    }

    /**
     * 查询执法检查记录列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnforceRecordReq req) {
        return enforceRecordService.selectEnforceRecordList(req);
    }

    /**
     * 导出执法检查记录列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnforceRecordReq req, HttpServletResponse response) {
        enforceRecordService.exportEnforceRecord(req, response);
    }

    /**
     * 新增执法检查记录
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) EnforceRecord info) {
        return enforceRecordService.insertEnforceRecord(info);
    }

    /**
     * 修改执法检查记录
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) EnforceRecord info) {
        return enforceRecordService.updateEnforceRecord(info);
    }

    /**
     * 删除执法检查记录
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EnforceRecord info) {
        return enforceRecordService.deleteEnforceRecord(info);
    }
}

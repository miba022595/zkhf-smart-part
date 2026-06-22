package com.zkhf.epmis.platform.envProtect.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeCheck;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;
import com.zkhf.epmis.platform.envProtect.service.EnvMangeCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业环评环保管理-环保验收Controller
 */
@RestController
@RequestMapping("/platform/manager/check")
public class EnvMangeCheckController {

    private EnvMangeCheckService envMangeCheckService;

    @Autowired
    public void setEnvMangeCheckService(EnvMangeCheckService envMangeCheckService) {
        this.envMangeCheckService = envMangeCheckService;
    }

    /**
     * 查询企业环评环保管理-环保验收关联的项目列表
     */
    @GetMapping("/relate/{checkId}")
    public AjaxResult checkRelate(@PathVariable String checkId) {
        return envMangeCheckService.checkRelateList(checkId);
    }

    /**
     * 查询企业环评环保管理-环保验收列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvMangeReq req) {
        return envMangeCheckService.selectMangeCheckList(req);
    }

    /**
     * 导出企业环评环保管理-v列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvMangeReq req, HttpServletResponse response) {
        envMangeCheckService.exportMangeCheck(req, response);
    }

    /**
     * 新增企业环评环保管理-环保验收
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvMangeCheck info) {
        return envMangeCheckService.insertMangeCheck(info);
    }

    /**
     * 修改企业环评环保管理-环保验收
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvMangeCheck info) {
        return envMangeCheckService.updateMangeCheck(info);
    }

    /**
     * 删除企业环评环保管理-环保验收
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") String id) {
        return envMangeCheckService.deleteMangeCheckById(id);
    }
}

package com.zkhf.epmis.platform.envProtect.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeEvaluate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;
import com.zkhf.epmis.platform.envProtect.service.EnvMangeEvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业环评环保管理-环评Controller
 */
@RestController
@RequestMapping("/platform/manager/evaluate")
public class EnvMangeEvaluateController {

    private EnvMangeEvaluateService envMangeEvaluateService;

    @Autowired
    public void setEnvMangeEvaluateService(EnvMangeEvaluateService envMangeEvaluateService) {
        this.envMangeEvaluateService = envMangeEvaluateService;
    }

    /**
     * 查询企业环评环保管理-环评关联的项目列表
     */
    @GetMapping("/relate/{evaluateId}")
    public AjaxResult evaluateRelate(@PathVariable String evaluateId) {
        return envMangeEvaluateService.evaluateRelateList(evaluateId);
    }

    /**
     * 查询企业环评环保管理-环评列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvMangeReq req) {
        return envMangeEvaluateService.selectMangeEvaluateList(req);
    }

    /**
     * 导出企业环评环保管理-环评列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvMangeReq req, HttpServletResponse response) {
        envMangeEvaluateService.exportMangeEvaluate(req, response);
    }

    /**
     * 新增企业环评环保管理-环评
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvMangeEvaluate info) {
        return envMangeEvaluateService.insertMangeEvaluate(info);
    }

    /**
     * 修改企业环评环保管理-环评
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvMangeEvaluate info) {
        return envMangeEvaluateService.updateMangeEvaluate(info);
    }

    /**
     * 删除企业环评环保管理-环评
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") String id) {
        return envMangeEvaluateService.deleteMangeEvaluateById(id);
    }
}

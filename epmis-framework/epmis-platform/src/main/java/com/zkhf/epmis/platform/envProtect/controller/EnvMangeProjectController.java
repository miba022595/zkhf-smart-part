package com.zkhf.epmis.platform.envProtect.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeProject;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;
import com.zkhf.epmis.platform.envProtect.service.EnvMangeProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业环评环保管理-项目Controller
 */
@RestController
@RequestMapping("/platform/manager/project")
public class EnvMangeProjectController {

    private EnvMangeProjectService envMangeProjectService;

    @Autowired
    public void setEnvMangeProjectService(EnvMangeProjectService envMangeProjectService) {
        this.envMangeProjectService = envMangeProjectService;
    }

    /**
     * 查询企业环评环保管理-项目关联的环评、环保验收列表
     */
    @GetMapping("/relate/{projectId}")
    public AjaxResult projectRelate(@PathVariable String projectId) {
        return envMangeProjectService.projectRelateList(projectId);
    }

    /**
     * 查询企业环评环保管理-项目列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvMangeReq req) {
        return envMangeProjectService.selectMangeProjectList(req);
    }

    /**
     * 导出企业环评环保管理-项目列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvMangeReq req, HttpServletResponse response) {
        envMangeProjectService.exportMangeProject(req, response);
    }

    /**
     * 新增企业环评环保管理-项目
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvMangeProject info) {
        return envMangeProjectService.insertMangeProject(info);
    }

    /**
     * 修改企业环评环保管理-项目
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvMangeProject info) {
        return envMangeProjectService.updateMangeProject(info);
    }

    /**
     * 删除企业环评环保管理-项目
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable("id") String id) {
        return envMangeProjectService.deleteMangeProjectById(id);
    }
}

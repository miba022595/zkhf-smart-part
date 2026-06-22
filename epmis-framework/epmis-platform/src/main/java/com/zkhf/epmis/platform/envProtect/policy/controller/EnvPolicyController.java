package com.zkhf.epmis.platform.envProtect.policy.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyInfo;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyReq;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环境政策法规信息-Controller
 */
@RestController
@RequestMapping("/platform/envPolicy")
public class EnvPolicyController {

    private EnvPolicyService envPolicyService;
    @Autowired
    public void setEnvPolicyService(EnvPolicyService envPolicyService) {
        this.envPolicyService = envPolicyService;
    }

    /**
     * 查询所有环境政策法规列表
     */
    @GetMapping("/listAll")
    public AjaxResult list() {
        return envPolicyService.envPolicyListAll();
    }

    /**
     * 查询环境政策法规列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvPolicyReq req) {
        return envPolicyService.selectEnvPolicyList(req);
    }

    /**
     * 导出环境政策法规列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvPolicyReq req, HttpServletResponse response) {
        envPolicyService.exportEnvPolicy(req, response);
    }

    /**
     * 新增环境政策法规
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvPolicyInfo info) {
        return envPolicyService.insertEnvPolicy(info);
    }

    /**
     * 修改环境政策法规
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvPolicyInfo info) {
        return envPolicyService.updateEnvPolicy(info);
    }

    /**
     * 删除环境政策法规
     */
	@DeleteMapping("/{policyId}")
    public AjaxResult remove(@PathVariable String policyId) {
        return envPolicyService.deleteEnvPolicyById(policyId);
    }
}

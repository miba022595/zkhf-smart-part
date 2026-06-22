package com.zkhf.epmis.platform.envProtect.policy.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestment;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestmentReq;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvInvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环保法规与体系管理-环保投入Controller
 */
@RestController
@RequestMapping("/platform/envInvestment")
public class EnvInvestmentController {

    private EnvInvestmentService envInvestmentService;

    @Autowired
    public void setEnvInvestmentService(EnvInvestmentService envInvestmentService) {
        this.envInvestmentService = envInvestmentService;
    }

    /**
     * 查询环保法规与体系管理-环保投入列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvInvestmentReq req) {
        return envInvestmentService.selectEnvInvestmentList(req);
    }

    /**
     * 导出环保法规与体系管理-环保投入
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvInvestmentReq req, HttpServletResponse response) {
        envInvestmentService.exportEnvInvestment(req, response);
    }

    /**
     * 新增环保法规与体系管理-环保投入
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvInvestment info) {
        return envInvestmentService.insertEnvInvestment(info);
    }

    /**
     * 修改环保法规与体系管理-环保投入
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvInvestment info) {
        return envInvestmentService.updateEnvInvestment(info);
    }

    /**
     * 删除环保法规与体系管理-环保投入
     */
    @DeleteMapping("/{investmentId}")
    public AjaxResult remove(@PathVariable String investmentId) {
        return envInvestmentService.deleteEnvInvestmentByInvestmentId(investmentId);
    }
}

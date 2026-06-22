package com.zkhf.epmis.platform.envProtect.policy.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupport;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupportReq;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchActual;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchPlan;
import com.zkhf.epmis.platform.envProtect.policy.service.GovernFundSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 政府资金支持Controller
 */
@RestController
@RequestMapping("/platform/governFundSupport")
public class GovernFundSupportController {

    private GovernFundSupportService governFundSupportService;
    @Autowired
    public void setGovernFundSupportService(GovernFundSupportService governFundSupportService) {
        this.governFundSupportService = governFundSupportService;
    }

    /**
     * 查询政府资金支持列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) GovernFundSupportReq req) {
        return governFundSupportService.selectGovernFundSupportList(req);
    }

    /**
     * 导出政府资金支持列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) GovernFundSupportReq req, HttpServletResponse response) {
        governFundSupportService.exportGovernFundSupport(req, response);
    }

    /**
     * 查询政府资金支持详情-计划批次+实际批次合并到一起展示
     */
    @GetMapping("/detail/{supportId}")
    public AjaxResult detail(@PathVariable String supportId) {
        return governFundSupportService.selectSupportDetail(supportId);
    }

    /**
     * 新增政府资金支持
     */
    @PostMapping
    public AjaxResult add(@RequestBody GovernFundSupport info) {
        return governFundSupportService.insertGovernFundSupport(info);
    }

    /**
     * 新增政府资金支持-计划批次
     */
    @PostMapping("/plan")
    public AjaxResult addPlan(@RequestBody SupportBatchPlan plan) {
        return governFundSupportService.insertGovernFundSupportP(plan);
    }

    /**
     * 新增政府资金支持-实际批次
     */
    @PostMapping("/actual")
    public AjaxResult addActual(@RequestBody SupportBatchActual actual) {
        return governFundSupportService.insertGovernFundSupportA(actual);
    }

    /**
     * 修改政府资金支持
     */
    @PutMapping
    public AjaxResult edit(@RequestBody GovernFundSupport info) {
        return governFundSupportService.updateGovernFundSupport(info);
    }

    /**
     * 修改政府资金支持-计划批次
     */
    @PutMapping("/plan")
    public AjaxResult editPlan(@RequestBody SupportBatchPlan plan) {
        return governFundSupportService.updateGovernFundSupportP(plan);
    }

    /**
     * 修改政府资金支持-实际批次
     */
    @PutMapping("/actual")
    public AjaxResult editActual(@RequestBody SupportBatchActual actual) {
        return governFundSupportService.updateGovernFundSupportA(actual);
    }

    /**
     * 删除政府资金支持
     */
	@DeleteMapping("/{supportId}")
    public AjaxResult remove(@PathVariable String supportId) {
        return governFundSupportService.deleteGovernFundSupportBySupportId(supportId);
    }

    /**
     * 删除政府资金支持-计划批次
     */
    @DeleteMapping("/plan/{id}")
    public AjaxResult removePlan(@PathVariable Long id) {
        return governFundSupportService.deleteGovernFundSupportPById(id);
    }

    /**
     * 删除政府资金支持-实际批次
     */
    @DeleteMapping("/actual/{id}")
    public AjaxResult removeActual(@PathVariable Long id) {
        return governFundSupportService.deleteGovernFundSupportAById(id);
    }
}

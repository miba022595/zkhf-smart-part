package com.zkhf.epmis.process.solidWaste.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteLibReq;
import com.zkhf.epmis.process.solidWaste.domain.WasteTotalPlan;
import com.zkhf.epmis.process.solidWaste.service.WasteTotalPlanService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 固废总量控制计划Controller
 */
@RestController
@RequestMapping("/process/wasteTotalPlan")
public class WasteTotalPlanController {

    private WasteTotalPlanService wasteTotalPlanService;
    @Autowired
    public void setWasteTotalPlanService(WasteTotalPlanService wasteTotalPlanService) {
        this.wasteTotalPlanService = wasteTotalPlanService;
    }

    /**
     * 查询固废总量控制计划列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) WasteLibReq req) {
        return wasteTotalPlanService.selectWasteTotalPlanList(req);
    }

    /**
     * 导出固废总量控制计划列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) WasteLibReq req, HttpServletResponse response) {
        wasteTotalPlanService.exportWasteTotalPlan(req, response);
    }

    /**
     * 新增固废总量控制计划
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) WasteTotalPlan info) {
        return wasteTotalPlanService.insertWasteTotalPlan(info);
    }

    /**
     * 修改固废总量控制计划
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody WasteTotalPlan info) {
        return wasteTotalPlanService.updateWasteTotalPlan(info);
    }

    /**
     * 删除固废总量控制计划
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) WasteTotalPlan info) {
        return wasteTotalPlanService.deleteWasteTotalPlan(info);
    }
}

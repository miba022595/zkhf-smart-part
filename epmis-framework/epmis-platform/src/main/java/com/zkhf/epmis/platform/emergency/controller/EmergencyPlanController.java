package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlan;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlanReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyPlanService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急预案控制器。
 * 提供应急预案的查询、维护、模板下载和导入导出接口。
 */
@Slf4j
@RestController
@RequestMapping("/platform/emergency/plan")
public class EmergencyPlanController {

    private EmergencyPlanService emergencyPlanService;
    @Autowired
    public void setEmergencyPlanService(EmergencyPlanService emergencyPlanService) {
        this.emergencyPlanService = emergencyPlanService;
    }

    /**
     * 获取应急预案列表
     * @param req 查询参数
     * @return 预案列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyPlanReq req) {
        return emergencyPlanService.list(req);
    }

    /**
     * 添加应急预案
     * @param info 预案信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) EmergencyPlan info) {
        return emergencyPlanService.add(info);
    }

    /**
     * 编辑应急预案
     * @param info 预案信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) EmergencyPlan info) {
        return emergencyPlanService.update(info);
    }

    /**
     * 删除应急预案
     * @param info 预案信息
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EmergencyPlan info) {
        return emergencyPlanService.delete(info);
    }

    /**
     * 导出应急预案。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) EmergencyPlanReq req, HttpServletResponse response) {
        emergencyPlanService.export(req, response);
    }

    /**
     * 下载应急预案导入模板
     * @param response 响应对象
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        emergencyPlanService.importTemplate(response);
    }

    /**
     * 导入应急预案
     * @param file Excel模板文件（.xlsx）
     * @return 导入结果，失败时返回具体行号错误
     */
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        return emergencyPlanService.importExcel(file);
    }
}

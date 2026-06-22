package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterial;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterialReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 应急物资控制器
 * 处理应急物资相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/platform/emergency/material")
public class EmergencyMaterialController {

    private EmergencyMaterialService emergencyMaterialService;
    @Autowired
    public void setEmergencyMaterialService(EmergencyMaterialService emergencyMaterialService) {
        this.emergencyMaterialService = emergencyMaterialService;
    }

    /**
     * 获取应急物资列表
     * @param req 查询参数
     * @return 物资列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyMaterialReq req) {
        return emergencyMaterialService.list(req);
    }

    /**
     * 添加应急物资
     * @param info 物资信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) EmergencyMaterial info) {
        return emergencyMaterialService.add(info);
    }

    /**
     * 编辑应急物资
     * @param info 物资信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) EmergencyMaterial info) {
        return emergencyMaterialService.update(info);
    }

    /**
     * 删除应急物资
     * @param info 物资信息
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EmergencyMaterial info) {
        return emergencyMaterialService.delete(info);
    }

    /**
     * 获取应急物资预警列表
     * @return 预警列表
     */
    @PostMapping("/warnList")
    public AjaxResult getWarnList() {
        return emergencyMaterialService.getWarnList();
    }

    /**
     * 导出应急物资列表
     * @param req 查询参数
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) EmergencyMaterialReq req, HttpServletResponse response) {
        emergencyMaterialService.export(response, req);
    }
}
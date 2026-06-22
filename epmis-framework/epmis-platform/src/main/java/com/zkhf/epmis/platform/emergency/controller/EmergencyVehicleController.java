package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyVehicle;
import com.zkhf.epmis.platform.emergency.domain.EmergencyVehicleReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyVehicleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急车辆控制器。
 * 提供应急车辆的查询、维护、模板下载和导入导出接口。
 */
@Slf4j
@RestController
@RequestMapping("/platform/emergency/vehicle")
public class EmergencyVehicleController {

    private EmergencyVehicleService emergencyVehicleService;
    @Autowired
    public void setEmergencyVehicleService(EmergencyVehicleService emergencyVehicleService) {
        this.emergencyVehicleService = emergencyVehicleService;
    }

    /**
     * 导出应急车辆。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) EmergencyVehicleReq req, HttpServletResponse response) {
        emergencyVehicleService.export(req, response);
    }

    /**
     * 下载应急车辆导入模板
     * @param response 响应对象
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        emergencyVehicleService.importTemplate(response);
    }

    /**
     * 导入应急车辆
     * @param file Excel模板文件（.xlsx）
     * @return 导入结果，失败时返回具体行号错误
     */
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        return emergencyVehicleService.importExcel(file);
    }

    /**
     * 获取应急车辆列表
     * @param req 查询参数
     * @return 车辆列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyVehicleReq req) {
        return emergencyVehicleService.list(req);
    }

    /**
     * 添加应急车辆
     * @param info 车辆信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) EmergencyVehicle info) {
        return emergencyVehicleService.add(info);
    }

    /**
     * 编辑应急车辆
     * @param info 车辆信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) EmergencyVehicle info) {
        return emergencyVehicleService.update(info);
    }

    /**
     * 删除应急车辆
     * @param info 车辆信息
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EmergencyVehicle info) {
        return emergencyVehicleService.delete(info);
    }
}

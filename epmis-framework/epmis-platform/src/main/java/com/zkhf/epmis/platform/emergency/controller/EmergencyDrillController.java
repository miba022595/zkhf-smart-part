package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyDrill;
import com.zkhf.epmis.platform.emergency.domain.EmergencyDrillReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyDrillService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急演练控制器。
 * 提供应急演练的查询、维护、模板下载和导入导出接口。
 */
@Slf4j
@RestController
@RequestMapping("/platform/emergency/drill")
public class EmergencyDrillController {

    private EmergencyDrillService emergencyDrillService;
    @Autowired
    public void setEmergencyDrillService(EmergencyDrillService emergencyDrillService) {
        this.emergencyDrillService = emergencyDrillService;
    }

    /**
     * 获取应急演练列表
     * @param req 查询参数
     * @return 演练列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyDrillReq req) {
        return emergencyDrillService.list(req);
    }

    /**
     * 添加应急演练
     * @param info 演练信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) EmergencyDrill info) {
        return emergencyDrillService.add(info);
    }

    /**
     * 编辑应急演练
     * @param info 演练信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) EmergencyDrill info) {
        return emergencyDrillService.update(info);
    }

    /**
     * 删除应急演练
     * @param info 演练信息
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EmergencyDrill info) {
        return emergencyDrillService.delete(info);
    }

    /**
     * 导出应急演练。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) EmergencyDrillReq req, HttpServletResponse response) {
        emergencyDrillService.export(req, response);
    }

    /**
     * 下载应急演练导入模板
     * @param response 响应对象
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        emergencyDrillService.importTemplate(response);
    }

    /**
     * 导入应急演练
     * @param file Excel模板文件（.xlsx）
     * @return 导入结果，失败时返回具体行号错误
     */
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        return emergencyDrillService.importExcel(file);
    }
}

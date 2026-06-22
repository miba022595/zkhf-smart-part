package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyExpert;
import com.zkhf.epmis.platform.emergency.domain.EmergencyExpertReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyExpertService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急专家控制器
 * 处理应急专家相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/platform/emergency/expert")
public class EmergencyExpertController {

    private EmergencyExpertService emergencyExpertService;
    @Autowired
    public void setEmergencyExpertService(EmergencyExpertService emergencyExpertService) {
        this.emergencyExpertService = emergencyExpertService;
    }

    /**
     * 导出应急专家列表
     * @param req 查询参数
     * @param response 响应对象
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) EmergencyExpertReq req, HttpServletResponse response) {
        emergencyExpertService.export(req, response);
    }

    /**
     * 下载应急专家导入模板
     * @param response 响应对象
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        emergencyExpertService.importTemplate(response);
    }

    /**
     * 导入应急专家
     * @param file Excel模板文件（.xlsx）
     * @return 导入结果，失败时返回具体行号错误
     */
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        return emergencyExpertService.importExcel(file);
    }

    /**
     * 获取应急专家列表
     * @param req 查询参数
     * @return 专家列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyExpertReq req) {
        return emergencyExpertService.list(req);
    }

    /**
     * 添加应急专家
     * @param info 专家信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) EmergencyExpert info) {
        return emergencyExpertService.add(info);
    }

    /**
     * 编辑应急专家
     * @param info 专家信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) EmergencyExpert info) {
        return emergencyExpertService.update(info);
    }

    /**
     * 删除应急专家
     * @param info 专家信息
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EmergencyExpert info) {
        return emergencyExpertService.delete(info);
    }
}

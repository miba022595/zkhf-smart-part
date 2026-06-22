package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyContact;
import com.zkhf.epmis.platform.emergency.domain.EmergencyContactReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyContactService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急组织机构控制器。
 * 当前主要提供应急通讯录的查询、维护、模板下载和导入导出接口。
 */
@RestController
@RequestMapping("/platform/emergency/org")
public class EmergencyOrgController {

    @Autowired
    private EmergencyContactService emergencyContactService;

    /**
     * 获取应急通讯录列表。
     *
     * @param req 查询条件
     * @return 列表结果
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyContactReq req) {
        return emergencyContactService.list(req);
    }

    /**
     * 添加应急联系人。
     *
     * @param info 联系人信息
     * @return 新增结果
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody EmergencyContact info) {
        return emergencyContactService.add(info);
    }

    /**
     * 编辑应急联系人。
     *
     * @param info 联系人信息
     * @return 更新结果
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody EmergencyContact info) {
        return emergencyContactService.update(info);
    }

    /**
     * 删除应急联系人。
     *
     * @param info 至少包含联系人ID
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody EmergencyContact info) {
        return emergencyContactService.delete(info);
    }

    /**
     * 导出应急通讯录。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) EmergencyContactReq req, HttpServletResponse response) {
        emergencyContactService.export(req, response);
    }

    /**
     * 下载应急通讯录导入模板。
     *
     * @param response HTTP响应
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        emergencyContactService.importTemplate(response);
    }

    /**
     * 导入应急通讯录
     * @param file Excel模板文件（.xlsx）
     * @return 导入结果，失败时返回具体行号错误
     */
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        return emergencyContactService.importExcel(file);
    }
}

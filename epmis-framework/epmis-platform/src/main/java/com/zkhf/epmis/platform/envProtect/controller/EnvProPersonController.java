package com.zkhf.epmis.platform.envProtect.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvProPerson;
import com.zkhf.epmis.platform.envProtect.domain.EnvProPersonReq;
import com.zkhf.epmis.platform.envProtect.service.EnvProPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业环保人员Controller
 */
@RestController
@RequestMapping("/platform/env/person")
public class EnvProPersonController {

    private EnvProPersonService envProPersonService;

    @Autowired
    public void setEnvProPersonService(EnvProPersonService envProPersonService) {
        this.envProPersonService = envProPersonService;
    }
    /**
     * 获取企业下的环保人员
     */
    @GetMapping(value = "/getProPersonByEnt")
    public AjaxResult selectProPersonByEnt(String entCode) {
        return envProPersonService.selectProPersonByEnt(entCode);
    }

    /**
     * 获取企业环保人员详细信息
     */
    @GetMapping(value = "/{envProPersonId}")
    public AjaxResult getInfo(@PathVariable("envProPersonId") String proPersonId) {
        return envProPersonService.selectProPersonById(proPersonId);
    }

    /**
     * 查询企业环保人员列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvProPersonReq req) {
        return envProPersonService.selectProPersonList(req);
    }

    /**
     * 导出企业环保人员列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvProPersonReq req, HttpServletResponse response) {
        envProPersonService.exportProPerson(req, response);
    }

    /**
     * 下载企业环保人员模板
     */
    @PostMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        envProPersonService.downloadTemplate(response);
    }

    /**
     * 导入企业环保人员模板
     */
    @PostMapping("/importTemplate")
    public AjaxResult importTemplate(@RequestParam("file") MultipartFile file, @RequestParam("entCode") String entCode) {
        return envProPersonService.importTemplate(file, entCode);
    }

    /**
     * 新增企业环保人员
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvProPerson info) {
        return envProPersonService.insertProPerson(info);
    }

    /**
     * 修改企业环保人员
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvProPerson info) {
        return envProPersonService.updateProPerson(info);
    }

    /**
     * 删除企业环保人员
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable List<String> ids) {
        return envProPersonService.deleteProPersonByIds(ids);
    }
}

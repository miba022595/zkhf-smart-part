package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialInfo;
import com.zkhf.epmis.process.material.domain.MaterialInfoReq;
import com.zkhf.epmis.process.material.service.MaterialInfoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 物资基础信息Controller
 */
@RestController
@RequestMapping("/process/materialInfo")
public class MaterialInfoController {

    private MaterialInfoService materialInfoService;
    @Autowired
    public void setMaterialInfoService(MaterialInfoService materialInfoService) {
        this.materialInfoService = materialInfoService;
    }

    /**
     * 查询物资基础信息列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialInfoReq req) {
        return materialInfoService.selectMaterialInfoList(req);
    }

    /**
     * 查询物资基础信息详情
     */
    @GetMapping("/detail/{materialId}")
    public AjaxResult detail(@PathVariable String materialId) {
        return materialInfoService.selectMaterialInfoDetail(materialId);
    }

    /**
     * 新增物资基础信息
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) MaterialInfo info) {
        return materialInfoService.insertMaterialInfo(info);
    }

    /**
     * 修改物资基础信息
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) MaterialInfo info) {
        return materialInfoService.updateMaterialInfo(info);
    }

    /**
     * 删除物资基础信息
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) MaterialInfo info) {
        return materialInfoService.deleteMaterialInfo(info);
    }

    /**
     * 导出物资基础信息
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) MaterialInfoReq req, HttpServletResponse response) {
        materialInfoService.exportMaterialInfo(req, response);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        materialInfoService.downloadMaterialInfoTemplate(response);
    }

    /**
     * 导入物资基础信息
     */
    @PostMapping("/import")
    public AjaxResult importExcel(@RequestParam("file") MultipartFile file) {
        return materialInfoService.importMaterialInfo(file);
    }
}

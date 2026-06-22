package com.zkhf.epmis.process.solidWaste.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategory;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategoryReq;
import com.zkhf.epmis.process.solidWaste.service.WasteCategoryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 固废种类管理Controller
 */
@RestController
@RequestMapping("/process/wasteCategory")
public class WasteCategoryController {

    private WasteCategoryService wasteCategoryService;
    @Autowired
    public void setWasteCategoryService(WasteCategoryService wasteCategoryService) {
        this.wasteCategoryService = wasteCategoryService;
    }

    /**
     * 查询固废种类管理列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) WasteCategoryReq req) {
        return wasteCategoryService.selectWasteCategoryList(req);
    }

    /**
     * 导出固废种类管理列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) WasteCategoryReq req, HttpServletResponse response) {
        wasteCategoryService.exportWasteCategory(req, response);
    }

    /**
     * 新增固废种类管理
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) WasteCategory info) {
        return wasteCategoryService.insertWasteCategory(info);
    }

    /**
     * 修改固废种类管理
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) WasteCategory info) {
        return wasteCategoryService.updateWasteCategory(info);
    }

    /**
     * 删除固废种类管理
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) WasteCategory info) {
        return wasteCategoryService.deleteWasteCategory(info);
    }
}

package com.zkhf.epmis.platform.ent.controller;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.ExtUnit;
import com.zkhf.epmis.platform.ent.domain.ExtUnitReq;
import com.zkhf.epmis.platform.ent.service.ExtUnitLibService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 第三方单位库Controller
 */
@RestController
@RequestMapping("/platform/extUnitLib")
public class ExtUnitLibController {

    private ExtUnitLibService extUnitLibService;
    @Autowired
    public void setExtUnitLibService(ExtUnitLibService extUnitLibService) {
        this.extUnitLibService = extUnitLibService;
    }

    /**
     * 查询第三方单位列表
     */
    @PostMapping("/allList")
    public AjaxResult allList(@RequestBody(required = false) ExtUnitReq req) {
        return extUnitLibService.selectExtUnitList(req);
    }

    /**
     * 查询第三方单位列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) ExtUnitReq req) {
        return extUnitLibService.selectExtUnitList(req);
    }

    /**
     * 导出第三方单位列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) ExtUnitReq req, HttpServletResponse response) {
        extUnitLibService.exportExtUnit(req, response);
    }

    /**
     * 新增第三方单位
     */
    @PostMapping
    public AjaxResult add(@RequestBody ExtUnit info) {
        return extUnitLibService.insertExtUnit(info);
    }

    /**
     * 修改第三方单位
     */
    @PutMapping
    public AjaxResult edit(@RequestBody ExtUnit info){
        return extUnitLibService.updateExtUnit(info);
    }

    /**
     * 修改第三方单位-用户的关联关系
     */
    @PutMapping("editExtUser")
    public AjaxResult editExtUser(@RequestBody JSONObject req){
        return extUnitLibService.editExtUser(req);
    }
}

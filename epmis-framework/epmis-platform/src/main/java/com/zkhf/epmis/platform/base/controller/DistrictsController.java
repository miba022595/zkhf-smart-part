package com.zkhf.epmis.platform.base.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.Districts;
import com.zkhf.epmis.platform.base.service.DistrictsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 地区Controller
 */
@RestController
@RequestMapping("/platform/districts")
public class DistrictsController {

    private DistrictsService districtsService;
    @Autowired
    public void setDistrictsService(DistrictsService districtsService) {
        this.districtsService = districtsService;
    }

    /**
     * 查询地区列表
     */
    @GetMapping("/singleList")
    public AjaxResult singleList(Long pid) {
        return districtsService.selectDistrictsSingleList(pid);
    }

    /**
     * 查询地区列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        return AjaxResult.success(districtsService.selectDistrictsList());
    }

    /**
     * 新增地区
     */
    @PostMapping
    public AjaxResult add(@RequestBody Districts info) {
        return districtsService.insertDistricts(info);
    }

    /**
     * 修改地区
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Districts info) {
        return districtsService.updateDistricts(info);
    }

    /**
     * 删除地区
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return districtsService.deleteDistrictsById(id);
    }
}

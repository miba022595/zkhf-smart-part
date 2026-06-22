package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialWarehouse;
import com.zkhf.epmis.process.material.domain.MaterialWarehouseReq;
import com.zkhf.epmis.process.material.service.MaterialWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仓库信息Controller
 */
@RestController
@RequestMapping("/process/materialWarehouse")
public class MaterialWarehouseController {

    private MaterialWarehouseService materialWarehouseService;
    @Autowired
    public void setMaterialWarehouseService(MaterialWarehouseService materialWarehouseService) {
        this.materialWarehouseService = materialWarehouseService;
    }

    /**
     * 查询仓库信息列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialWarehouseReq req) {
        return materialWarehouseService.selectMaterialWarehouseList(req);
    }

    /**
     * 新增仓库信息
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) MaterialWarehouse info) {
        return materialWarehouseService.insertMaterialWarehouse(info);
    }

    /**
     * 修改仓库信息
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) MaterialWarehouse info) {
        return materialWarehouseService.updateMaterialWarehouse(info);
    }

    /**
     * 删除仓库信息
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) MaterialWarehouse info) {
        return materialWarehouseService.deleteMaterialWarehouse(info);
    }
}

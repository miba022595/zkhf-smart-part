package com.zkhf.epmis.process.solidWaste.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.*;
import com.zkhf.epmis.process.solidWaste.service.WasteStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 固废库存管理Controller
 * 包含产生、减量、入库、出库的记录管理
 */
@RestController
@RequestMapping("/process/wasteStock")
public class WasteStockController {

    private WasteStockService wasteStockService;
    @Autowired
    public void setWasteStockService(WasteStockService wasteStockService) {
        this.wasteStockService = wasteStockService;
    }

    /**
     * 查询固废产生记录列表
     */
    @PostMapping("/listGenerate")
    public AjaxResult listGenerate(@RequestBody(required = false) WasteLibReq req) {
        return wasteStockService.selectWasteGenerateList(req);
    }

    /**
     * 新增固废产生记录
     */
    @PostMapping("/addGenerate")
    public AjaxResult addGenerate(@RequestBody(required = false) WasteGenerate info) {
        return wasteStockService.insertWasteGenerate(info);
    }

    /**
     * 删除固废产生记录
     */
    @PostMapping("/removeGenerate")
    public AjaxResult removeGenerate(@RequestBody(required = false) WasteGenerate req) {
        return wasteStockService.deleteWasteGenerate(req);
    }

    /**
     * 查询固废减量记录列表
     */
    @PostMapping("/listReduction")
    public AjaxResult listReduction(@RequestBody(required = false) WasteLibReq req) {
        return wasteStockService.selectWasteReductionList(req);
    }

    /**
     * 新增固废减量记录
     */
    @PostMapping("/addReduction")
    public AjaxResult addReduction(@RequestBody(required = false) WasteReduction info) {
        return wasteStockService.insertWasteReduction(info);
    }

    /**
     * 删除固废减量记录
     */
    @PostMapping("/removeReduction")
    public AjaxResult removeReduction(@RequestBody(required = false) WasteReduction req) {
        return wasteStockService.deleteWasteReduction(req);
    }

    /**
     * 查询固废入库记录列表
     */
    @PostMapping("/listStorage")
    public AjaxResult listStorage(@RequestBody(required = false) WasteLibReq req) {
        return wasteStockService.selectWasteStorageList(req);
    }

    /**
     * 新增固废入库记录
     */
    @PostMapping("/addStorage")
    public AjaxResult addStorage(@RequestBody(required = false) WasteStorage info) {
        return wasteStockService.insertWasteStorage(info);
    }

    /**
     * 删除固废入库记录
     */
    @PostMapping("/removeStorage")
    public AjaxResult removeStorage(@RequestBody(required = false) WasteStorage req) {
        return wasteStockService.deleteWasteStorage(req);
    }

    /**
     * 查询固废出库记录列表
     */
    @PostMapping("/listOutbound")
    public AjaxResult listOutbound(@RequestBody(required = false) WasteLibReq req) {
        return wasteStockService.selectWasteOutboundList(req);
    }

    /**
     * 新增固废出库记录
     */
    @PostMapping("/addOutbound")
    public AjaxResult addOutbound(@RequestBody(required = false) WasteOutbound info) {
        return wasteStockService.insertWasteOutbound(info);
    }

    /**
     * 删除固废出库记录
     */
    @PostMapping("/removeOutbound")
    public AjaxResult removeOutbound(@RequestBody(required = false) WasteOutbound req) {
        return wasteStockService.deleteWasteOutbound(req);
    }
}

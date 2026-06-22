package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialReturnOrder;
import com.zkhf.epmis.process.material.service.MaterialReturnOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 物资归还单Controller
 */
@RestController
@RequestMapping("/process/materialReturnOrder")
public class MaterialReturnOrderController {

    private MaterialReturnOrderService materialReturnOrderService;
    @Autowired
    public void setMaterialReturnOrderService(MaterialReturnOrderService materialReturnOrderService) {
        this.materialReturnOrderService = materialReturnOrderService;
    }

    /**
     * 查询物资归还单列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialBizReq req) { return materialReturnOrderService.selectMaterialReturnOrderList(req); }

    /**
     * 查询物资归还单详情
     */
    @GetMapping("/detail/{returnId}")
    public AjaxResult detail(@PathVariable String returnId) { return materialReturnOrderService.selectMaterialReturnOrderDetail(returnId); }

    /**
     * 新增物资归还单
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) MaterialReturnOrder info) { return materialReturnOrderService.insertMaterialReturnOrder(info); }

    /**
     * 修改物资归还单
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) MaterialReturnOrder info) { return materialReturnOrderService.updateMaterialReturnOrder(info); }

    /**
     * 删除物资归还单
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) MaterialReturnOrder info) { return materialReturnOrderService.deleteMaterialReturnOrder(info); }

    /**
     * 导出物资归还单
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) MaterialBizReq req, HttpServletResponse response) {
        materialReturnOrderService.exportMaterialReturnOrder(req, response);
    }
}

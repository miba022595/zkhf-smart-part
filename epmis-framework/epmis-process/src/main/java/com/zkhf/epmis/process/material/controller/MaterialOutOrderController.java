package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialOutOrder;
import com.zkhf.epmis.process.material.service.MaterialOutOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 物资出库单Controller
 */
@RestController
@RequestMapping("/process/materialOutOrder")
public class MaterialOutOrderController {

    private MaterialOutOrderService materialOutOrderService;
    @Autowired
    public void setMaterialOutOrderService(MaterialOutOrderService materialOutOrderService) {
        this.materialOutOrderService = materialOutOrderService;
    }

    /**
     * 查询物资出库单列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialBizReq req) { return materialOutOrderService.selectMaterialOutOrderList(req); }

    /**
     * 查询物资出库单详情
     */
    @GetMapping("/detail/{outId}")
    public AjaxResult detail(@PathVariable String outId) { return materialOutOrderService.selectMaterialOutOrderDetail(outId); }

    /**
     * 新增物资出库单
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) MaterialOutOrder info) { return materialOutOrderService.insertMaterialOutOrder(info); }

    /**
     * 修改物资出库单
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) MaterialOutOrder info) { return materialOutOrderService.updateMaterialOutOrder(info); }

    /**
     * 删除物资出库单
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) MaterialOutOrder info) { return materialOutOrderService.deleteMaterialOutOrder(info); }

    /**
     * 导出物资出库单
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) MaterialBizReq req, HttpServletResponse response) {
        materialOutOrderService.exportMaterialOutOrder(req, response);
    }
}

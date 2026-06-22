package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialInOrder;
import com.zkhf.epmis.process.material.service.MaterialInOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 物资入库单Controller
 */
@RestController
@RequestMapping("/process/materialInOrder")
public class MaterialInOrderController {

    private MaterialInOrderService materialInOrderService;
    @Autowired
    public void setMaterialInOrderService(MaterialInOrderService materialInOrderService) {
        this.materialInOrderService = materialInOrderService;
    }

    /**
     * 查询物资入库单列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialBizReq req) { return materialInOrderService.selectMaterialInOrderList(req); }

    /**
     * 查询物资入库单详情
     */
    @GetMapping("/detail/{inId}")
    public AjaxResult detail(@PathVariable String inId) { return materialInOrderService.selectMaterialInOrderDetail(inId); }

    /**
     * 新增物资入库单
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) MaterialInOrder info) { return materialInOrderService.insertMaterialInOrder(info); }

    /**
     * 修改物资入库单
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) MaterialInOrder info) { return materialInOrderService.updateMaterialInOrder(info); }

    /**
     * 删除物资入库单
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) MaterialInOrder info) { return materialInOrderService.deleteMaterialInOrder(info); }

    /**
     * 导出物资入库单
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) MaterialBizReq req, HttpServletResponse response) {
        materialInOrderService.exportMaterialInOrder(req, response);
    }
}

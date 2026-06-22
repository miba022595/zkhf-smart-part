package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialApplyOrder;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.service.MaterialApplyOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 物资申请单Controller
 */
@RestController
@RequestMapping("/process/materialApplyOrder")
public class MaterialApplyOrderController {

    private MaterialApplyOrderService materialApplyOrderService;
    @Autowired
    public void setMaterialApplyOrderService(MaterialApplyOrderService materialApplyOrderService) {
        this.materialApplyOrderService = materialApplyOrderService;
    }

    /**
     * 查询物资申请单列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialBizReq req) { return materialApplyOrderService.selectMaterialApplyOrderList(req); }

    /**
     * 查询物资申请单详情
     */
    @GetMapping("/detail/{applyId}")
    public AjaxResult detail(@PathVariable String applyId) { return materialApplyOrderService.selectMaterialApplyOrderDetail(applyId); }

    /**
     * 新增物资申请单
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) MaterialApplyOrder info) { return materialApplyOrderService.insertMaterialApplyOrder(info); }

    /**
     * 修改物资申请单
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) MaterialApplyOrder info) { return materialApplyOrderService.updateMaterialApplyOrder(info); }

    /**
     * 删除物资申请单
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) MaterialApplyOrder info) { return materialApplyOrderService.deleteMaterialApplyOrder(info); }

    /**
     * 导出
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) MaterialBizReq req, HttpServletResponse response) { materialApplyOrderService.exportMaterialApplyOrder(req, response); }
}

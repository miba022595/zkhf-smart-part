package com.zkhf.epmis.process.material.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialStockFlowReq;
import com.zkhf.epmis.process.material.domain.MaterialStockReq;
import com.zkhf.epmis.process.material.service.MaterialStockService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 物资库存Controller
 */
@RestController
@RequestMapping("/process/materialStock")
public class MaterialStockController {

    private MaterialStockService materialStockService;
    @Autowired
    public void setMaterialStockService(MaterialStockService materialStockService) {
        this.materialStockService = materialStockService;
    }

    /**
     * 查询物资库存汇总列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) MaterialStockReq req) {
        return materialStockService.selectMaterialStockList(req);
    }

    /**
     * 查询物资库存流水列表
     */
    @PostMapping("/detail/list")
    public AjaxResult detailList(@RequestBody(required = false) MaterialStockFlowReq req) {
        return materialStockService.selectMaterialStockFlowList(req);
    }

    /**
     * 导出物资库存汇总
     */
    @PostMapping("/export")
    public void export(@RequestBody(required = false) MaterialStockReq req, HttpServletResponse response) {
        materialStockService.exportMaterialStock(req, response);
    }

    /**
     * 导出物资库存流水
     */
    @PostMapping("/detail/export")
    public void exportDetail(@RequestBody(required = false) MaterialStockFlowReq req, HttpServletResponse response) {
        materialStockService.exportMaterialStockFlow(req, response);
    }
}

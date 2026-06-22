package com.zkhf.epmis.platform.ent.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshop;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshopReq;
import com.zkhf.epmis.platform.ent.service.EntProduceWorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业生产车间Controller
 */
@RestController
@RequestMapping("/platform/ent/workshop")
public class EntProduceWorkshopController {

    private EntProduceWorkshopService entProduceWorkshopService;
    @Autowired
    public void setEntProduceWorkshopService(EntProduceWorkshopService entProduceWorkshopService) {
        this.entProduceWorkshopService = entProduceWorkshopService;
    }

    /**
     * 查询企业生产车间列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EntProduceWorkshopReq req) {
        return entProduceWorkshopService.selectEntProduceWorkshopList(req);
    }

    /**
     * 导出企业生产车间列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EntProduceWorkshopReq req, HttpServletResponse response) {
        entProduceWorkshopService.exportEntProduceWorkshop(req, response);
    }

    /**
     * 新增企业生产车间
     */
    @PostMapping
    public AjaxResult add(@RequestBody EntProduceWorkshop info) {
        return entProduceWorkshopService.insertEntProduceWorkshop(info);
    }

    /**
     * 修改企业生产车间
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EntProduceWorkshop info) {
        return entProduceWorkshopService.updateEntProduceWorkshop(info);
    }

    /**
     * 删除企业生产车间
     */
    @DeleteMapping("/{workshopId}")
    public AjaxResult remove(@PathVariable String workshopId) {
        return entProduceWorkshopService.deleteEntProduceWorkshopById(workshopId);
    }
}

package com.zkhf.epmis.platform.ent.controller;

import jakarta.servlet.http.HttpServletResponse;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntProductionLine;
import com.zkhf.epmis.platform.ent.domain.EntProductionLineReq;
import com.zkhf.epmis.platform.ent.service.EntProductionLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业生产线信息Controller
 */
@RestController
@RequestMapping("/platform/line")
public class EntProductionLineController {

    private EntProductionLineService entProductionLineService;
    @Autowired
    public void setEntProductionLineService(EntProductionLineService entProductionLineService) {
        this.entProductionLineService = entProductionLineService;
    }

    /**
     * 获取企业生产线信息详细信息
     */
    @GetMapping(value = "/detail/{lineId}")
    public AjaxResult getDetail(@PathVariable("lineId") String lineId) {
        return entProductionLineService.selectEntProductionLineDetailByLineId(lineId);
    }

    /**
     * 查询企业生产线信息列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EntProductionLineReq req) {
        return entProductionLineService.selectEntProductionLineList(req);
    }

    /**
     * 导出企业生产线信息列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EntProductionLineReq req, HttpServletResponse response) {
        entProductionLineService.exportEntProductionLine(req, response);
    }

    /**
     * 新增企业生产线信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody EntProductionLine line) {
        return entProductionLineService.insertEntProductionLine(line);
    }

    /**
     * 修改企业生产线信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EntProductionLine line) {
        return entProductionLineService.updateEntProductionLine(line);
    }

    /**
     * 删除企业生产线信息
     */
    @DeleteMapping("/{lineId}")
    public AjaxResult remove(@PathVariable String lineId) {
        return entProductionLineService.deleteEntProductionLineById(lineId);
    }
}

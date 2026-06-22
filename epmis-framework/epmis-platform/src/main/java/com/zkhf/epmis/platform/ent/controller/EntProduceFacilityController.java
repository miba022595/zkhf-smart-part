package com.zkhf.epmis.platform.ent.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacility;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacilityReq;
import com.zkhf.epmis.platform.ent.service.EntProduceFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业生产设施/设备Controller
 */
@RestController
@RequestMapping("/platform/ent/produceFacility")
public class EntProduceFacilityController {

    private EntProduceFacilityService entProduceFacilityService;
    @Autowired
    public void setEntProduceFacilityService(EntProduceFacilityService entProduceFacilityService) {
        this.entProduceFacilityService = entProduceFacilityService;
    }

    /**
     * 查询企业生产设施/设备列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EntProduceFacilityReq req) {
        return entProduceFacilityService.selectEntProduceFacilityList(req);
    }

    /**
     * 导出企业生产设施/设备列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EntProduceFacilityReq req, HttpServletResponse response) {
        entProduceFacilityService.exportEntProduceFacility(req, response);
    }

    /**
     * 新增企业生产设施/设备
     */
    @PostMapping
    public AjaxResult add(@RequestBody EntProduceFacility info) {
        return entProduceFacilityService.insertEntProduceFacility(info);
    }

    /**
     * 修改企业生产设施/设备
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EntProduceFacility info) {
        return entProduceFacilityService.updateEntProduceFacility(info);
    }

    /**
     * 删除企业生产设施/设备
     */
    @DeleteMapping("/{facilityId}")
    public AjaxResult remove(@PathVariable String facilityId) {
        return entProduceFacilityService.deleteEntProduceFacilityById(facilityId);
    }
}

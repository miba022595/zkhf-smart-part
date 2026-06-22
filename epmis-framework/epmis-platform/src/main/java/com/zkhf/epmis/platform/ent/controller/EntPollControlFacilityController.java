package com.zkhf.epmis.platform.ent.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacility;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacilityReq;
import com.zkhf.epmis.platform.ent.service.EntPollControlFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业污染治理设施Controller
 */
@RestController
@RequestMapping("/platform/ent/pollControlFacility")
public class EntPollControlFacilityController {

    private EntPollControlFacilityService entPollControlFacilityService;
    @Autowired
    public void setEntPollControlFacilityService(EntPollControlFacilityService entPollControlFacilityService) {
        this.entPollControlFacilityService = entPollControlFacilityService;
    }

    /**
     * 查询企业污染治理设施列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EntPollControlFacilityReq req) {
        return entPollControlFacilityService.selectEntPollControlFacilityList(req);
    }

    /**
     * 导出企业污染治理设施列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EntPollControlFacilityReq req, HttpServletResponse response) {
        entPollControlFacilityService.exportEntPollControlFacility(req, response);
    }

    /**
     * 新增企业污染治理设施
     */
    @PostMapping
    public AjaxResult add(@RequestBody EntPollControlFacility info) {
        return entPollControlFacilityService.insertEntPollControlFacility(info);
    }

    /**
     * 修改企业污染治理设施
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EntPollControlFacility info) {
        return entPollControlFacilityService.updateEntPollControlFacility(info);
    }

    /**
     * 删除企业污染治理设施
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable String id) {
        return entPollControlFacilityService.deleteEntPollControlFacilityById(id);
    }
}

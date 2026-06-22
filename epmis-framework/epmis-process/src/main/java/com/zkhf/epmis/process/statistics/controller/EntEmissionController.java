package com.zkhf.epmis.process.statistics.controller;

import com.zkhf.epmis.process.statistics.domain.EmissionReq;
import com.zkhf.epmis.process.statistics.service.EntEmissionService;
import com.zkhf.epmis.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业年排量信息记录Controller
 */
@RestController
@RequestMapping("/process/emission")
public class EntEmissionController {

    private EntEmissionService entEmissionService;

    @Autowired
    public void setEntAnnualOutputInfoService(EntEmissionService entEmissionService) {
        this.entEmissionService = entEmissionService;
    }

    /**
     * 查询企业年排量信息记录列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmissionReq req) {
        return entEmissionService.selectEntEmissionList(req);
    }

    /**
     * 查询排口年排量信息记录列表
     * 指定企业、年份、污染物code
     */
    @PostMapping("/out/list")
    public AjaxResult outEmissionList(@RequestBody(required = false) EmissionReq req) {
        return entEmissionService.selectOutEmissionList(req);
    }

    /**
     * 导出企业年排量信息记录列表
     * 按模板导出
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EmissionReq req, HttpServletResponse response) {
        entEmissionService.exportEntEmission(req, response);
    }
}

package com.zkhf.epmis.process.valid.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.valid.domain.ValidPeriodReq;
import com.zkhf.epmis.process.valid.service.ValidPeriodInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业资质有效期预警数据Controller
 */
@RestController
@RequestMapping("/process/validPeriod")
public class ValidPeriodInfoController {

    private ValidPeriodInfoService validPeriodInfoService;
    @Autowired
    public void setValidPeriodInfoService(ValidPeriodInfoService validPeriodInfoService) {
        this.validPeriodInfoService = validPeriodInfoService;
    }

    /**
     * 查询资质证件类型
     */
    @GetMapping("/confType")
    public AjaxResult confType() {
        return validPeriodInfoService.selectConfType();
    }

    /**
     * 查询企业资质有效期预警数据列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) ValidPeriodReq req) {
        return validPeriodInfoService.selectValidPeriodInfoList(req);
    }

    /**
     * 导出企业资质有效期预警数据列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) ValidPeriodReq req, HttpServletResponse response) {
        validPeriodInfoService.exportValidPeriodInfo(req, response);
    }
}

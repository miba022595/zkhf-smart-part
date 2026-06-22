package com.zkhf.epmis.process.statistics.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.statistics.domain.EffectiveTransReq;
import com.zkhf.epmis.process.statistics.service.EffectiveTransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业数据有效率信息统计Controller
 */
@RestController
@RequestMapping("/process/effTrans")
public class EffectiveTransController {

    private EffectiveTransService effectiveTransService;

    @Autowired
    public void setEffectiveTransService(EffectiveTransService effectiveTransService) {
        this.effectiveTransService = effectiveTransService;
    }

    /**
     * 查询企业数据有效率信息统计列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EffectiveTransReq req) {
        return effectiveTransService.selectEffectiveTransList(req);
    }

    /**
     * 导出企业数据有效率信息统计列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EffectiveTransReq req, HttpServletResponse response) {
        effectiveTransService.exportEffectiveTrans(req, response);
    }
}

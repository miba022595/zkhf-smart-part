package com.zkhf.epmis.platform.base.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.PollutantCodeReq;
import com.zkhf.epmis.platform.base.service.PollutantCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数采报文对应的污染因子关系 2017版本和2003版Controller
 */
@RestController
@RequestMapping("/platform/base/pollutantCode")
public class PollutantCodeController {

    private PollutantCodeService pollutantCodeService;
    @Autowired
    public void setPollutantCodeService(PollutantCodeService pollutantCodeService) {
        this.pollutantCodeService = pollutantCodeService;
    }

    /**
     * 查询数采报文对应的污染因子关系 2017版本和2003版列表
     */
    @GetMapping("/list")
    public AjaxResult list(PollutantCodeReq req) {
        return pollutantCodeService.selectPollutantCodeList(req);
    }
}

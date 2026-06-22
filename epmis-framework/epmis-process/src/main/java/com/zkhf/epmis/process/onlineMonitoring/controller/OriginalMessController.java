package com.zkhf.epmis.process.onlineMonitoring.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.onlineMonitoring.domain.OriginalMessReq;
import com.zkhf.epmis.process.onlineMonitoring.service.OriginalMessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 原始报文数据 Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/originalMess")
public class OriginalMessController {

    private OriginalMessService originalMessService;
    @Autowired
    public void setOriginalMessService(OriginalMessService originalMessService) {
        this.originalMessService = originalMessService;
    }

    /**
     * 原始报文数据列表查询
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) OriginalMessReq req) {
        return originalMessService.selectList(req);
    }
}

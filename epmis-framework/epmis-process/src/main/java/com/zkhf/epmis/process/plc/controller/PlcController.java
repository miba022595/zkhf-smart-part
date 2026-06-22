package com.zkhf.epmis.process.plc.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.plc.domain.PlcReq;
import com.zkhf.epmis.process.plc.service.PlcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企业plc信息 Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/plc")
public class PlcController {

    private PlcService plcService;
    @Autowired
    public void setPlcService(PlcService plcService) {
        this.plcService = plcService;
    }

    /**
     * 获取企业下排口的实时工况数据
     */
    @PostMapping("/realtime")
    public AjaxResult getRealTimeData(@RequestBody(required = false) PlcReq req) {
        return plcService.getRealTimeData(req);
    }

    /**
     * 获取企业下排口的历史工况数据
     */
    @PostMapping("/history")
    public AjaxResult getHistoryData(@RequestBody(required = false) PlcReq req) {
        return plcService.getHistoryData(req);
    }
}

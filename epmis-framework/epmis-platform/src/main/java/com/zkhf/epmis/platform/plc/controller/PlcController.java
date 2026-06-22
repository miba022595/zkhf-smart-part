package com.zkhf.epmis.platform.plc.controller;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.plc.domain.PlcInfo;
import com.zkhf.epmis.platform.plc.service.PlcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * PLC设备点位信息Controller Controller
 */
@Slf4j
@RestController
@RequestMapping("/platform/plc")
public class PlcController {

    private PlcService plcService;
    @Autowired
    public void setPlcService(PlcService plcService) {
        this.plcService = plcService;
    }

    /**
     * 查询企业PLC设备点位列表
     */
    @PostMapping("/list")
    public AjaxResult plcList(@RequestBody(required = false) JSONObject req) {
        return plcService.selectEntPlcList(req);
    }

    /**
     * 导出企业PLC设备点位列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) JSONObject req, HttpServletResponse response) {
        plcService.exportEntPlcList(req, response);
    }

    /**
     * 更新企业PLC设备单个点位
     */
    @PutMapping("/single")
    public AjaxResult edit(@RequestBody(required = false) PlcInfo plc) {
        return plcService.updateEntPlc(plc);
    }

    /**
     * 更新企业的PLC设备点位
     */
    @PutMapping
    public AjaxResult edit(@RequestBody(required = false) JSONObject req) {
        return plcService.updateEntPlc(req);
    }
}

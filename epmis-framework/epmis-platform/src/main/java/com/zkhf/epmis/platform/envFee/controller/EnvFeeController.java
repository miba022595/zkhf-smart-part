package com.zkhf.epmis.platform.envFee.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFee;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeReq;
import com.zkhf.epmis.platform.envFee.service.EnvFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环保费用登记Controller
 */
@RestController
@RequestMapping("/platform/fees")
public class EnvFeeController {

    private EnvFeeService envFeeService;
    @Autowired
    public void setEnvFeesService(EnvFeeService envFeeService) {
        this.envFeeService = envFeeService;
    }

    /**
     * 查询环保费用登记列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvFeeReq req) {
        return envFeeService.selectEnvFeeList(req);
    }

    /**
     * 导出环保费用登记列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvFeeReq req, HttpServletResponse response) {
        envFeeService.exportEnvFee(req, response);
    }

    /**
     * 新增环保费用登记
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvFee info) {
        return envFeeService.insertEnvFee(info);
    }

    /**
     * 修改环保费用登记
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvFee info) {
        return envFeeService.updateEnvFee(info);
    }

    /**
     * 删除环保费用登记
     */
    @DeleteMapping("/{feeId}")
    public AjaxResult remove(@PathVariable String feeId) {
        return envFeeService.deleteEnvFeeById(feeId);
    }
}

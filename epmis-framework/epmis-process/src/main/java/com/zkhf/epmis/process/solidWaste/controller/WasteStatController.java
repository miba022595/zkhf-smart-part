package com.zkhf.epmis.process.solidWaste.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteLibReq;
import com.zkhf.epmis.process.solidWaste.service.WasteStatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 固废统计Controller
 */
@RestController
@RequestMapping("/process/wasteStat")
public class WasteStatController {

    private WasteStatService wasteStatService;
    @Autowired
    public void setTWasteMonthlyStatsService(WasteStatService wasteStatService) {
        this.wasteStatService = wasteStatService;
    }

    /**
     * 查询固废总量统计汇总列表(总台账)
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) WasteLibReq req) {
        return wasteStatService.selectStatList(req);
    }

    /**
     * 导出固废总量统计汇总列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) WasteLibReq req, HttpServletResponse response) {
        wasteStatService.exportStat(req, response);
    }
}

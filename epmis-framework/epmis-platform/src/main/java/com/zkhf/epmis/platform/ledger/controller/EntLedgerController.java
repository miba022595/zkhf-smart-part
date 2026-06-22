package com.zkhf.epmis.platform.ledger.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ledger.service.EntLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业台账 Controller
 */
@RestController
@RequestMapping("/platform/ledger/ent")
public class EntLedgerController {

    private EntLedgerService entLedgerService;
    @Autowired
    public void setDeviceInfoService(EntLedgerService entLedgerService) {
        this.entLedgerService = entLedgerService;
    }

    /**
     * 监测设施台账
     */
    @GetMapping(value = "/monitorFacility/list")
    public AjaxResult monitorFacilityList() {
        return entLedgerService.monitorFacilityList();
    }

    /**
     * 导出监测设施台账
     */
    @PostMapping("/monitorFacility/exportTemplate")
    public void monitorFacilityExportTemplate(HttpServletResponse response) {
        entLedgerService.monitorFacilityExport(response);
    }

    /**
     * 治理设施台账
     */
    @GetMapping("/governFacility/list")
    public AjaxResult governFacilityList() {
        return entLedgerService.governFacilityList();
    }

    /**
     * 导出治理设施台账
     */
    @PostMapping("/governFacility/exportTemplate")
    public void governFacilityExportTemplate(HttpServletResponse response) {
        entLedgerService.governFacilityExport(response);
    }
}

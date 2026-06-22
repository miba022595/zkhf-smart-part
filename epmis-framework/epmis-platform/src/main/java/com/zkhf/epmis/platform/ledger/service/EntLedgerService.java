package com.zkhf.epmis.platform.ledger.service;

import com.zkhf.epmis.core.domain.AjaxResult;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业台账 Service接口
 */
public interface EntLedgerService {

    /**
     * 监测设施台账
     */
    AjaxResult monitorFacilityList();

    /**
     * 导出监测设施台账
     */
    void monitorFacilityExport(HttpServletResponse response);

    /**
     * 治理设施台账
     */
    AjaxResult governFacilityList();

    /**
     * 导出治理设施台账
     */
    void governFacilityExport(HttpServletResponse response);
}

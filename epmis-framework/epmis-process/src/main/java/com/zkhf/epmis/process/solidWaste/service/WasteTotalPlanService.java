package com.zkhf.epmis.process.solidWaste.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteLibReq;
import com.zkhf.epmis.process.solidWaste.domain.WasteTotalPlan;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 固废总量控制计划Service接口
 */
public interface WasteTotalPlanService {

    /**
     * 查询固废总量控制计划列表
     */
    AjaxResult selectWasteTotalPlanList(WasteLibReq req);

    /**
     * 导出固废总量控制计划列表
     */
    void exportWasteTotalPlan(WasteLibReq req, HttpServletResponse response);

    /**
     * 新增固废总量控制计划
     */
    AjaxResult insertWasteTotalPlan(WasteTotalPlan info);

    /**
     * 修改固废总量控制计划
     */
    AjaxResult updateWasteTotalPlan(WasteTotalPlan info);

    /**
     * 删除固废总量控制计划
     */
    AjaxResult deleteWasteTotalPlan(WasteTotalPlan info);
}

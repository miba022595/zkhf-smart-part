package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialStockFlowReq;
import com.zkhf.epmis.process.material.domain.MaterialStockReq;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 物资库存Service接口
 */
public interface MaterialStockService {

    /**
     * 查询物资库存汇总列表
     */
    AjaxResult selectMaterialStockList(MaterialStockReq req);

    /**
     * 查询物资库存流水明细列表
     */
    AjaxResult selectMaterialStockFlowList(MaterialStockFlowReq req);

    /**
     * 导出物资库存汇总
     */
    void exportMaterialStock(MaterialStockReq req, HttpServletResponse response);

    /**
     * 导出物资库存流水明细
     */
    void exportMaterialStockFlow(MaterialStockFlowReq req, HttpServletResponse response);
}

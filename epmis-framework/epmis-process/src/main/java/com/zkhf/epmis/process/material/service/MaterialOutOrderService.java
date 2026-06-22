package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialOutOrder;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 物资出库单Service接口
 */
public interface MaterialOutOrderService {

    /**
     * 查询物资出库单列表
     */
    AjaxResult selectMaterialOutOrderList(MaterialBizReq req);
    /**
     * 查询物资出库单详情
     */
    AjaxResult selectMaterialOutOrderDetail(String outId);
    /**
     * 新增物资出库单
     */
    AjaxResult insertMaterialOutOrder(MaterialOutOrder info);
    /**
     * 修改物资出库单
     */
    AjaxResult updateMaterialOutOrder(MaterialOutOrder info);
    /**
     * 删除物资出库单
     */
    AjaxResult deleteMaterialOutOrder(MaterialOutOrder info);

    /**
     * 导出物资出库单
     */
    void exportMaterialOutOrder(MaterialBizReq req, HttpServletResponse response);
}

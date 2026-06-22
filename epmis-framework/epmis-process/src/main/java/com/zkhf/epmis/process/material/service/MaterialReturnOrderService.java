package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialReturnOrder;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 物资归还单Service接口
 */
public interface MaterialReturnOrderService {

    /**
     * 查询物资归还单列表
     */
    AjaxResult selectMaterialReturnOrderList(MaterialBizReq req);

    /**
     * 查询物资归还单详情
     */
    AjaxResult selectMaterialReturnOrderDetail(String returnId);

    /**
     * 新增物资归还单
     */
    AjaxResult insertMaterialReturnOrder(MaterialReturnOrder info);

    /**
     * 修改物资归还单
     */
    AjaxResult updateMaterialReturnOrder(MaterialReturnOrder info);

    /**
     * 删除物资归还单
     */
    AjaxResult deleteMaterialReturnOrder(MaterialReturnOrder info);

    /**
     * 导出物资归还单
     */
    void exportMaterialReturnOrder(MaterialBizReq req, HttpServletResponse response);
}

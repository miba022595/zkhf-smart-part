package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialInOrder;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 物资入库单Service接口
 */
public interface MaterialInOrderService {

    /**
     * 查询物资入库单列表
     */
    AjaxResult selectMaterialInOrderList(MaterialBizReq req);

    /**
     * 查询物资入库单详情
     */
    AjaxResult selectMaterialInOrderDetail(String inId);

    /**
     * 新增物资入库单
     */
    AjaxResult insertMaterialInOrder(MaterialInOrder info);

    /**
     * 修改物资入库单
     */
    AjaxResult updateMaterialInOrder(MaterialInOrder info);

    /**
     * 删除物资入库单
     */
    AjaxResult deleteMaterialInOrder(MaterialInOrder info);

    /**
     * 导出物资入库单
     */
    void exportMaterialInOrder(MaterialBizReq req, HttpServletResponse response);
}

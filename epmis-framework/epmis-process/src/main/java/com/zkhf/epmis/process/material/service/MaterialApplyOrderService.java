package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialApplyOrder;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 物资申请单Service接口
 */
public interface MaterialApplyOrderService {

    /**
     * 查询物资申请单列表
     */
    AjaxResult selectMaterialApplyOrderList(MaterialBizReq req);

    /**
     * 查询物资申请单详情
     */
    AjaxResult selectMaterialApplyOrderDetail(String applyId);

    /**
     * 新增物资申请单
     */
    AjaxResult insertMaterialApplyOrder(MaterialApplyOrder info);

    /**
     * 修改物资申请单
     */
    AjaxResult updateMaterialApplyOrder(MaterialApplyOrder info);

    /**
     * 删除物资申请单
     */
    AjaxResult deleteMaterialApplyOrder(MaterialApplyOrder info);

    /**
     * 导出
     */
    void exportMaterialApplyOrder(MaterialBizReq req, HttpServletResponse response);
}

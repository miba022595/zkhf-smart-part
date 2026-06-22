package com.zkhf.epmis.process.solidWaste.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategory;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategoryReq;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 固废种类管理Service接口
 */
public interface WasteCategoryService {

    /**
     * 查询固废种类
     */
    WasteCategory selectWasteCategoryById(String categoryId);

    /**
     * 查询固废种类管理列表
     */
    AjaxResult selectWasteCategoryList(WasteCategoryReq req);

    /**
     * 导出固废种类管理列表
     */
    void exportWasteCategory(WasteCategoryReq req, HttpServletResponse response);

    /**
     * 新增固废种类管理
     */
    AjaxResult insertWasteCategory(WasteCategory info);

    /**
     * 修改固废种类管理
     */
    AjaxResult updateWasteCategory(WasteCategory info);

    /**
     * 删除固废种类管理信息
     */
    AjaxResult deleteWasteCategory(WasteCategory info);
}

package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshop;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshopReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业生产车间Service接口
 */
public interface EntProduceWorkshopService {

    /**
     * 查询企业生产车间列表
     */
    AjaxResult selectEntProduceWorkshopList(EntProduceWorkshopReq req);

    /**
     * 导出企业生产车间列表
     */
    void exportEntProduceWorkshop(EntProduceWorkshopReq req, HttpServletResponse response);

    /**
     * 新增企业生产车间
     */
    AjaxResult insertEntProduceWorkshop(EntProduceWorkshop info);

    /**
     * 修改企业生产车间
     */
    AjaxResult updateEntProduceWorkshop(EntProduceWorkshop info);

    /**
     * 删除企业生产车间信息
     */
    AjaxResult deleteEntProduceWorkshopById(String workshopId);
}

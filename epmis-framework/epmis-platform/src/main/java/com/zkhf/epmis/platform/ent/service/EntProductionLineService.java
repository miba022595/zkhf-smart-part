package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntProductionLine;
import com.zkhf.epmis.platform.ent.domain.EntProductionLineReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业生产线信息Service接口
 */
public interface EntProductionLineService {

    /**
     * 查询企业生产线详情信息
     */
    AjaxResult selectEntProductionLineDetailByLineId(String lineId);

    /**
     * 查询企业生产线信息列表
     */
    AjaxResult selectEntProductionLineList(EntProductionLineReq req);

    /**
     * 导出企业生产线信息列表
     */
    void exportEntProductionLine(EntProductionLineReq req, HttpServletResponse response);

    /**
     * 新增企业生产线信息
     */
    AjaxResult insertEntProductionLine(EntProductionLine line);

    /**
     * 修改企业生产线信息
     */
    AjaxResult updateEntProductionLine(EntProductionLine line);

    /**
     * 删除企业生产线信息信息
     */
    AjaxResult deleteEntProductionLineById(String lineId);
}

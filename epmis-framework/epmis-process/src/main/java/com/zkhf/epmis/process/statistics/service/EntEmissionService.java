package com.zkhf.epmis.process.statistics.service;

import com.zkhf.epmis.process.statistics.domain.EmissionReq;
import com.zkhf.epmis.core.domain.AjaxResult;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业年排量信息记录Service接口
 */
public interface EntEmissionService {

    /**
     * 查询企业年排量信息记录列表
     */
    AjaxResult selectEntEmissionList(EmissionReq req);

    /**
     * 查询排口年排量信息列表
     */
    AjaxResult selectOutEmissionList(EmissionReq req);

    /**
     * 导出企业年排量信息记录列表
     * 按模板导出
     */
    void exportEntEmission(EmissionReq req, HttpServletResponse response);
}

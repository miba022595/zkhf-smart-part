package com.zkhf.epmis.process.valid.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.valid.domain.ValidPeriodReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业资质有效期预警数据Service接口
 */
public interface ValidPeriodInfoService {

    /**
     * 查询资质证件类型
     */
    AjaxResult selectConfType();

    /**
     * 查询企业资质有效期预警数据列表
     */
    AjaxResult selectValidPeriodInfoList(ValidPeriodReq req);

    /**
     * 导出企业资质有效期预警数据列表
     */
    void exportValidPeriodInfo(ValidPeriodReq req, HttpServletResponse response);
}

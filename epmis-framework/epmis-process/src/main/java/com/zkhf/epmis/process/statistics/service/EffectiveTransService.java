package com.zkhf.epmis.process.statistics.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.statistics.domain.EffectiveTransReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业数据有效率信息统计Service接口
 */
public interface EffectiveTransService {

    /**
     * 查询企业数据有效率信息统计列表
     */
    AjaxResult selectEffectiveTransList(EffectiveTransReq req);

    /**
     * 导出企业数据有效率信息统计列表
     */
    void exportEffectiveTrans(EffectiveTransReq req, HttpServletResponse response);
}

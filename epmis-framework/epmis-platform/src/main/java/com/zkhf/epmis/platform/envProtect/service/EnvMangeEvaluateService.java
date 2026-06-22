package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeEvaluate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业环评环保管理-环评Service接口
 */
public interface EnvMangeEvaluateService {

    /**
     * 查询企业环评环保管理-环评关联的项目列表
     */
    AjaxResult evaluateRelateList(String evaluateId);

    /**
     * 查询企业环评环保管理-环评列表
     */
    AjaxResult selectMangeEvaluateList(EnvMangeReq req);

    /**
     * 导出企业环评环保管理-环评列表
     */
    void exportMangeEvaluate(EnvMangeReq req, HttpServletResponse response);

    /**
     * 新增企业环评环保管理-环评
     */
    AjaxResult insertMangeEvaluate(EnvMangeEvaluate info);

    /**
     * 修改企业环评环保管理-环评
     */
    AjaxResult updateMangeEvaluate(EnvMangeEvaluate info);

    /**
     * 删除企业环评环保管理-环评信息
     */
    AjaxResult deleteMangeEvaluateById(String id);
}

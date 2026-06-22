package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeProject;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业环评环保管理-项目Service接口
 */
public interface EnvMangeProjectService {

    /**
     * 查询企业环评环保管理-项目关联的环评、环保验收列表
     */
    AjaxResult projectRelateList(String projectId);

    /**
     * 查询企业环评环保管理-项目列表
     */
    AjaxResult selectMangeProjectList(EnvMangeReq req);

    /**
     * 导出企业环评环保管理-项目列表
     */
    void exportMangeProject(EnvMangeReq req, HttpServletResponse response);

    /**
     * 新增企业环评环保管理-项目
     */
    AjaxResult insertMangeProject(EnvMangeProject info);

    /**
     * 修改企业环评环保管理-项目
     */
    AjaxResult updateMangeProject(EnvMangeProject info);

    /**
     * 删除企业环评环保管理-项目信息
     */
    AjaxResult deleteMangeProjectById(String id);
}

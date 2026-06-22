package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeCheck;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业环评环保管理-环保验收Service接口
 */
public interface EnvMangeCheckService {

    /**
     * 查询企业环评环保管理-环保验收关联的项目
     */
    AjaxResult checkRelateList(String checkId);

    /**
     * 查询企业环评环保管理-环保验收列表
     */
    AjaxResult selectMangeCheckList(EnvMangeReq req);

    /**
     * 导出企业环评环保管理-环保验收列表
     */
    void exportMangeCheck(EnvMangeReq req, HttpServletResponse response);

    /**
     * 新增企业环评环保管理-环保验收
     */
    AjaxResult insertMangeCheck(EnvMangeCheck info);

    /**
     * 修改企业环评环保管理-环保验收
     */
    AjaxResult updateMangeCheck(EnvMangeCheck info);

    /**
     * 删除企业环评环保管理-环保验收信息
     */
    AjaxResult deleteMangeCheckById(String id);
}

package com.zkhf.epmis.platform.envProtect.policy.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyInfo;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环境政策法规信息-Service接口
 */
public interface EnvPolicyService {

    String pubSign = "-1";

    /**
     * 查询所有环境政策法规列表
     */
    AjaxResult envPolicyListAll();

    /**
     * 查询环境政策法规列表
     */
    AjaxResult selectEnvPolicyList(EnvPolicyReq req);

    /**
     * 导出环境政策法规列表
     */
    void exportEnvPolicy(EnvPolicyReq req, HttpServletResponse response);

    /**
     * 新增环境政策法规
     */
    AjaxResult insertEnvPolicy(EnvPolicyInfo info);

    /**
     * 修改环境政策法规
     */
    AjaxResult updateEnvPolicy(EnvPolicyInfo info);

    /**
     * 删除环境政策法规
     */
    AjaxResult deleteEnvPolicyById(String policyId);
}

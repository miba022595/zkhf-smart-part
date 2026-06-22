package com.zkhf.epmis.platform.envProtect.policy.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestment;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestmentReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 环保法规与体系管理-环保投入Service接口
 */
public interface EnvInvestmentService {

    /**
     * 查询环保法规与体系管理-环保投入列表
     */
    AjaxResult selectEnvInvestmentList(EnvInvestmentReq req);

    /**
     * 导出环保法规与体系管理-环保投入列表
     */
    void exportEnvInvestment(EnvInvestmentReq req, HttpServletResponse response);

    /**
     * 新增环保法规与体系管理-环保投入
     */
    AjaxResult insertEnvInvestment(EnvInvestment info);

    /**
     * 修改环保法规与体系管理-环保投入
     */
    AjaxResult updateEnvInvestment(EnvInvestment info);

    /**
     * 删除环保法规与体系管理-环保投入信息
     */
    AjaxResult deleteEnvInvestmentByInvestmentId(String investmentId);
}

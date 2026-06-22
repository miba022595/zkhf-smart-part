package com.zkhf.epmis.platform.envProtect.policy.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupport;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupportReq;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchActual;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchPlan;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 政府资金支持Service接口
 */
public interface GovernFundSupportService {

    /**
     * 查询政府资金支持列表
     */
    AjaxResult selectGovernFundSupportList(GovernFundSupportReq req);

    /**
     * 导出政府资金支持列表
     */
    void exportGovernFundSupport(GovernFundSupportReq req, HttpServletResponse response);

    /**
     * 查询政府资金支持详情-计划列表+实际列表
     */
    AjaxResult selectSupportDetail(String supportId);

    /**
     * 新增政府资金支持
     */
    AjaxResult insertGovernFundSupport(GovernFundSupport info);

    /**
     * 新增政府资金支持-计划批次
     */
    AjaxResult insertGovernFundSupportP(SupportBatchPlan plan);

    /**
     * 新增政府资金支持-实际批次
     */
    AjaxResult insertGovernFundSupportA(SupportBatchActual actual);

    /**
     * 修改政府资金支持
     */
    AjaxResult updateGovernFundSupport(GovernFundSupport info);

    /**
     * 修改政府资金支持-计划批次
     */
    AjaxResult updateGovernFundSupportP(SupportBatchPlan plan);

    /**
     * 修改政府资金支持-实际批次
     */
    AjaxResult updateGovernFundSupportA(SupportBatchActual actual);

    /**
     * 删除政府资金支持信息
     */
    AjaxResult deleteGovernFundSupportBySupportId(String supportId);

    /**
     * 删除政府资金支持信息-计划批次
     */
    AjaxResult deleteGovernFundSupportPById(Long id);

    /**
     * 删除政府资金支持信息-实际批次
     */
    AjaxResult deleteGovernFundSupportAById(Long id);
}

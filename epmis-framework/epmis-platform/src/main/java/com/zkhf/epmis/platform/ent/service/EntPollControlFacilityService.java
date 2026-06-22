package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacility;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacilityReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业污染治理设施Service接口
 */
public interface EntPollControlFacilityService {

    /**
     * 查询企业污染治理设施列表
     */
    AjaxResult selectEntPollControlFacilityList(EntPollControlFacilityReq req);

    /**
     * 导出企业污染治理设施列表
     */
    void exportEntPollControlFacility(EntPollControlFacilityReq req, HttpServletResponse response);

    /**
     * 新增企业污染治理设施
     */
    AjaxResult insertEntPollControlFacility(EntPollControlFacility info);

    /**
     * 修改企业污染治理设施
     */
    AjaxResult updateEntPollControlFacility(EntPollControlFacility info);

    /**
     * 删除企业污染治理设施信息
     */
    AjaxResult deleteEntPollControlFacilityById(String id);
}

package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacility;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacilityReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业生产设施/设备Service接口
 */
public interface EntProduceFacilityService {

    /**
     * 查询企业生产设施/设备列表
     */
    AjaxResult selectEntProduceFacilityList(EntProduceFacilityReq req);

    /**
     * 导出企业生产设施/设备列表
     */
    void exportEntProduceFacility(EntProduceFacilityReq req, HttpServletResponse response);

    /**
     * 新增企业生产设施/设备
     */
    AjaxResult insertEntProduceFacility(EntProduceFacility info);

    /**
     * 修改企业生产设施/设备
     */
    AjaxResult updateEntProduceFacility(EntProduceFacility info);

    /**
     * 删除企业生产设施/设备信息
     */
    AjaxResult deleteEntProduceFacilityById(String facilityId);
}

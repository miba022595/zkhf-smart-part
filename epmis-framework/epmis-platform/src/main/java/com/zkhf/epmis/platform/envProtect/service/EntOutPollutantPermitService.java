package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermit;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitCount;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitCountReq;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 企业排污许可基础Service接口
 */
public interface EntOutPollutantPermitService {

    /**
     * 查询指定年份的企业排污许可总量
     */
    List<Map<String, Object>> selectAllEntOutPollutantPermitCount(Integer permitYear);

    /**
     * 查询企业排污许可基础列表
     */
    AjaxResult selectEntOutPollutantPermitList(EntOutPollutantPermitReq req);

    /**
     * 导出企业排污许可基础列表
     */
    void exportEntOutPollutantPermit(EntOutPollutantPermitReq req, HttpServletResponse response);

    /**
     * 修改企业排污许可基础
     */
    AjaxResult updateEntOutPollutantPermit(EntOutPollutantPermit permit);

    /**
     * 查询企业排污许可总量数据列表
     */
    AjaxResult selectEntOutPollutantPermitCountList(EntOutPollutantPermitCountReq req);

    /**
     * 导出企业排污许可总量数据列表
     */
    void exportEntOutPollutantPermitCount(EntOutPollutantPermitCountReq req, HttpServletResponse response);

    /**
     * 新增企业排污许可总量数据
     */
    AjaxResult insertEntOutPollutantPermitCount(EntOutPollutantPermitCount permit);

    /**
     * 修改企业排污许可总量数据
     */
    AjaxResult updateEntOutPollutantPermitCount(EntOutPollutantPermitCount permit);

    /**
     * 批量删除企业排污许可总量数据
     */
    AjaxResult deleteEntOutPollutantPermitCountByPollPermitIds(List<String> pollPermitIds);
}

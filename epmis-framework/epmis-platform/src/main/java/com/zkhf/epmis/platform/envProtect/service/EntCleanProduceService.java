package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EntCleanProduce;
import com.zkhf.epmis.platform.envProtect.domain.EntCleanProduceReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 企业清洁生产基础Service接口
 */
public interface EntCleanProduceService {

    /**
     * 查询企业清洁生产详情
     */
    AjaxResult selectCleanProduceById(String cleanProduceId);

    /**
     * 查询企业清洁生产基础列表
     */
    AjaxResult selectCleanProduceList(EntCleanProduceReq req);

    /**
     * 导出企业清洁生产基础列表
     */
    void exportCleanProduce(EntCleanProduceReq req, HttpServletResponse response);

    /**
     * 新增企业清洁生产基础
     */
    AjaxResult insertCleanProduce(EntCleanProduce produce);

    /**
     * 修改企业清洁生产基础
     */
    AjaxResult updateCleanProduce(EntCleanProduce produce);

    /**
     * 批量删除企业清洁生产基础
     */
    AjaxResult deleteCleanProduceByIds(List<String> ids);
}

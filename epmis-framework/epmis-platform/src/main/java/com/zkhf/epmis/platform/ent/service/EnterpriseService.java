package com.zkhf.epmis.platform.ent.service;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.Enterprise;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.ent.domain.EnterpriseReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 企业基础Service接口
 */
public interface EnterpriseService {

    /**
     * 查询企业列表
     */
    List<EnterprisePart> listAll();

    /**
     * 查询企业列表-排口类型筛选
     */
    AjaxResult listByPollType(Integer pollType);

    /**
     * 查询企业列表-企业所属地区筛选
     */
    AjaxResult listByRegion(String region);

    /**
     * 查询企业全量列表-只要部分字段
     */
    AjaxResult selectPartListAll();

    /**
     * 企业下的排口树
     */
    AjaxResult outPutTree(JSONObject req);

    /**
     * 企业下的排口树-按企业分层级
     */
    AjaxResult outPutTreeByEnt(Integer pollType);

    /**
     * 企业下的排口树-按所属地区分层级
     */
    AjaxResult outPutTreeByRegion();

    /**
     * 查询企业基础列表
     */
    AjaxResult selectList(EnterpriseReq req);

    /**
     * 导出企业基础列表
     */
    void exportList(EnterpriseReq req, HttpServletResponse response);

    /**
     * 新增企业基础
     */
    AjaxResult insertEnterprise(Enterprise info);

    /**
     * 修改企业基础
     */
    AjaxResult updateEnterprise(Enterprise info);

    /**
     * 删除企业基础
     */
    AjaxResult deleteEnterpriseByEntCode(String entCode);

    /**
     * 内部调用获取企业列表
     */
    List<EnterprisePart> selectPartListAllByInternal(String entCode);

    /**
     * 单个企业爬取
     */
    AjaxResult entLicenseSpider(String entCode);
}

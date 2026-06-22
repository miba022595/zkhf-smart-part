package com.zkhf.epmis.platform.ent.service;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.ExtUnit;
import com.zkhf.epmis.platform.ent.domain.ExtUnitReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 第三方单位库Service接口
 */
public interface ExtUnitLibService {
    /**
     * 查询所有第三方单位列表
     */
    List<ExtUnit> selectAllExtUnitList();

    /**
     * 查询第三方单位列表
     */
    AjaxResult selectExtUnitList(ExtUnitReq req);

    /**
     * 导出第三方单位列表
     * 按模板导出
     */
    void exportExtUnit(ExtUnitReq req, HttpServletResponse response);

    /**
     * 新增第三方单位
     */
    AjaxResult insertExtUnit(ExtUnit info);

    /**
     * 修改第三方单位
     */
    AjaxResult updateExtUnit(ExtUnit info);

    /**
     * 修改第三方单位-用户的关联关系
     */
    AjaxResult editExtUser(JSONObject req);
}
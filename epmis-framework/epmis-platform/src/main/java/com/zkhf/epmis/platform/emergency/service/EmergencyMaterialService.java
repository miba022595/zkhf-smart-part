package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterial;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterialReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 应急物资服务接口
 * 提供应急物资的增删改查、预警和导出等操作
 */
public interface EmergencyMaterialService {

    /**
     * 获取应急物资列表
     * @param req 查询参数
     * @return 物资列表
     */
    AjaxResult list(EmergencyMaterialReq req);

    /**
     * 添加应急物资
     * @param info 物资信息
     * @return 添加结果
     */
    AjaxResult add(EmergencyMaterial info);

    /**
     * 更新应急物资信息
     * @param info 物资信息
     * @return 更新结果
     */
    AjaxResult update(EmergencyMaterial info);

    /**
     * 删除应急物资
     * @param info 物资信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyMaterial info);

    /**
     * 获取应急物资预警列表
     * @return 预警列表
     */
    AjaxResult getWarnList();

    /**
     * 导出应急物资列表
     * @param response HTTP响应
     * @param req 查询参数
     */
    void export(HttpServletResponse response, EmergencyMaterialReq req);

    /**
     * 扫描并更新物资质保预警状态
     */
    void scanWarnStatus();
}
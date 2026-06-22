package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlan;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlanReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急预案服务接口。
 * 提供应急预案列表查询、维护、模板下载、Excel导入导出能力。
 */
public interface EmergencyPlanService {

    /**
     * 获取应急预案列表
     * @param req 查询参数
     * @return 预案列表
     */
    AjaxResult list(EmergencyPlanReq req);

    /**
     * 添加应急预案
     * @param info 预案信息
     * @return 添加结果
     */
    AjaxResult add(EmergencyPlan info);

    /**
     * 更新应急预案信息
     * @param info 预案信息
     * @return 更新结果
     */
    AjaxResult update(EmergencyPlan info);

    /**
     * 删除应急预案
     * @param info 预案信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyPlan info);

    /**
     * 导出应急预案。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    void export(EmergencyPlanReq req, HttpServletResponse response);

    /**
     * 下载应急预案导入模板。
     *
     * @param response HTTP响应
     */
    void importTemplate(HttpServletResponse response);

    /**
     * 按模板导入应急预案数据。
     *
     * @param file Excel文件
     * @return 导入结果
     */
    AjaxResult importExcel(MultipartFile file);
}

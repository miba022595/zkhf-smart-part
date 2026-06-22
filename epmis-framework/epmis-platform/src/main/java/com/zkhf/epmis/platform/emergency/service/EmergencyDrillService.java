package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyDrill;
import com.zkhf.epmis.platform.emergency.domain.EmergencyDrillReq;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急演练服务接口。
 * 提供应急演练列表查询、维护、模板下载、Excel导入导出能力。
 */
public interface EmergencyDrillService {

    /**
     * 获取应急演练列表
     * @param req 查询参数
     * @return 演练列表
     */
    AjaxResult list(EmergencyDrillReq req);

    /**
     * 添加应急演练
     * @param info 演练信息
     * @return 添加结果
     */
    AjaxResult add(EmergencyDrill info);

    /**
     * 更新应急演练信息
     * @param info 演练信息
     * @return 更新结果
     */
    AjaxResult update(EmergencyDrill info);

    /**
     * 删除应急演练
     * @param info 演练信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyDrill info);

    /**
     * 导出应急演练。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    void export(EmergencyDrillReq req, HttpServletResponse response);

    /**
     * 下载应急演练导入模板。
     *
     * @param response HTTP响应
     */
    void importTemplate(HttpServletResponse response);

    /**
     * 按模板导入应急演练数据。
     *
     * @param file Excel文件
     * @return 导入结果
     */
    AjaxResult importExcel(MultipartFile file);
}

package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyExpert;
import com.zkhf.epmis.platform.emergency.domain.EmergencyExpertReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急专家服务接口。
 * 提供应急专家列表查询、维护、模板下载、Excel导入导出能力。
 */
public interface EmergencyExpertService {

    /**
     * 获取应急专家列表
     * @param req 查询参数
     * @return 专家列表
     */
    AjaxResult list(EmergencyExpertReq req);

    /**
     * 添加应急专家
     * @param info 专家信息
     * @return 添加结果
     */
    AjaxResult add(EmergencyExpert info);

    /**
     * 更新应急专家信息
     * @param info 专家信息
     * @return 更新结果
     */
    AjaxResult update(EmergencyExpert info);

    /**
     * 删除应急专家
     * @param info 专家信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyExpert info);

    /**
     * 导出应急专家。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    void export(EmergencyExpertReq req, HttpServletResponse response);

    /**
     * 下载应急专家导入模板。
     *
     * @param response HTTP响应
     */
    void importTemplate(HttpServletResponse response);

    /**
     * 按模板导入应急专家数据。
     *
     * @param file Excel文件
     * @return 导入结果
     */
    AjaxResult importExcel(MultipartFile file);
}

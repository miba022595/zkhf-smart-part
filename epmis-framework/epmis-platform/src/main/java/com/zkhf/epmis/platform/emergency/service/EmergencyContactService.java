package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyContact;
import com.zkhf.epmis.platform.emergency.domain.EmergencyContactReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急通讯录服务接口。
 * 提供应急联系人列表查询、维护、模板下载、Excel导入导出能力。
 */
public interface EmergencyContactService {

    /**
     * 分页查询应急通讯录。
     *
     * @param req 查询条件
     * @return 列表结果
     */
    AjaxResult list(EmergencyContactReq req);

    /**
     * 新增应急联系人。
     *
     * @param info 联系人信息
     * @return 新增结果
     */
    AjaxResult add(EmergencyContact info);

    /**
     * 修改应急联系人。
     *
     * @param info 联系人信息
     * @return 更新结果
     */
    AjaxResult update(EmergencyContact info);

    /**
     * 删除应急联系人。
     *
     * @param info 联系人标识信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyContact info);

    /**
     * 导出应急通讯录。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    void export(EmergencyContactReq req, HttpServletResponse response);

    /**
     * 下载应急通讯录导入模板。
     *
     * @param response HTTP响应
     */
    void importTemplate(HttpServletResponse response);

    /**
     * 按模板导入应急通讯录数据。
     *
     * @param file Excel文件
     * @return 导入结果
     */
    AjaxResult importExcel(MultipartFile file);
}

package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyVehicle;
import com.zkhf.epmis.platform.emergency.domain.EmergencyVehicleReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 应急车辆服务接口。
 * 提供应急车辆列表查询、维护、模板下载、Excel导入导出能力。
 */
public interface EmergencyVehicleService {

    /**
     * 获取应急车辆列表
     * @param req 查询参数
     * @return 车辆列表
     */
    AjaxResult list(EmergencyVehicleReq req);

    /**
     * 添加应急车辆
     * @param info 车辆信息
     * @return 添加结果
     */
    AjaxResult add(EmergencyVehicle info);

    /**
     * 更新应急车辆信息
     * @param info 车辆信息
     * @return 更新结果
     */
    AjaxResult update(EmergencyVehicle info);

    /**
     * 删除应急车辆
     * @param info 车辆信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyVehicle info);

    /**
     * 导出应急车辆。
     *
     * @param req 查询条件
     * @param response HTTP响应
     */
    void export(EmergencyVehicleReq req, HttpServletResponse response);

    /**
     * 下载应急车辆导入模板。
     *
     * @param response HTTP响应
     */
    void importTemplate(HttpServletResponse response);

    /**
     * 按模板导入应急车辆数据。
     *
     * @param file Excel文件
     * @return 导入结果
     */
    AjaxResult importExcel(MultipartFile file);
}

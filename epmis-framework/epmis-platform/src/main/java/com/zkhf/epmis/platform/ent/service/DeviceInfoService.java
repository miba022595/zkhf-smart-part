package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.DeviceInfo;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 设备信息Service接口
 */
public interface DeviceInfoService {

    /**
     * 查询设备信息
     */
    AjaxResult selectDeviceInfoByMnNum(String mnNum);

    /**
     * 查询设备信息列表
     */
    AjaxResult selectDeviceInfoList();

    /**
     * 导出设备信息列表
     */
    void exportDeviceInfo(HttpServletResponse response);

    /**
     * 新增设备信息
     */
    AjaxResult insertOrUpdateDeviceInfo(DeviceInfo info);
}

package com.zkhf.epmis.platform.ent.controller;

import jakarta.servlet.http.HttpServletResponse;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.DeviceInfo;
import com.zkhf.epmis.platform.ent.service.DeviceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备信息Controller
 */
@RestController
@RequestMapping("/platform/device")
public class DeviceInfoController {

    private DeviceInfoService deviceInfoService;
    @Autowired
    public void setDeviceInfoService(DeviceInfoService deviceInfoService) {
        this.deviceInfoService = deviceInfoService;
    }

    /**
     * 获取设备信息详细信息
     */
    @GetMapping(value = "/{mnNum}")
    public AjaxResult getInfo(@PathVariable("mnNum") String mnNum) {
        return deviceInfoService.selectDeviceInfoByMnNum(mnNum);
    }

    /**
     * 查询设备信息列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        return deviceInfoService.selectDeviceInfoList();
    }

    /**
     * 导出设备信息列表
     */
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletResponse response) {
        deviceInfoService.exportDeviceInfo(response);
    }

    /**
     * 新增设备信息
     */
    @PostMapping
    public AjaxResult insertOrUpdateDeviceInfo(@RequestBody DeviceInfo info) {
        return deviceInfoService.insertOrUpdateDeviceInfo(info);
    }
}

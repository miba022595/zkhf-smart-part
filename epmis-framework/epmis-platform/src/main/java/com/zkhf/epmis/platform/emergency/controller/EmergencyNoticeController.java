package com.zkhf.epmis.platform.emergency.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNotice;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNoticeReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应急通知控制器
 * 处理应急通知相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/platform/emergency/notice")
public class EmergencyNoticeController {

    private EmergencyNoticeService emergencyNoticeService;
    @Autowired
    public void setEmergencyNoticeService(EmergencyNoticeService emergencyNoticeService) {
        this.emergencyNoticeService = emergencyNoticeService;
    }

    /**
     * 获取应急通知列表
     * @param req 查询参数
     * @return 通知列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EmergencyNoticeReq req) {
        return emergencyNoticeService.list(req);
    }

    /**
     * 发送应急通知
     * @param info 通知信息
     * @return 发送结果
     */
    @PostMapping("/send")
    public AjaxResult send(@RequestBody(required = false) EmergencyNotice info) {
        return emergencyNoticeService.send(info);
    }

    /**
     * 删除应急通知
     * @param info 通知信息
     * @return 删除结果
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) EmergencyNotice info) {
        return emergencyNoticeService.delete(info);
    }
}
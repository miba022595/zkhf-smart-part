package com.zkhf.epmis.platform.emergency.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNotice;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNoticeReq;

import java.util.List;

/**
 * 应急通知服务接口
 * 提供应急通知的发送、查询和删除等操作
 */
public interface EmergencyNoticeService {

    /**
     * 获取应急通知列表
     * @param req 查询参数
     * @return 通知列表
     */
    AjaxResult list(EmergencyNoticeReq req);

    /**
     * 发送应急通知
     * @param info 通知信息
     * @return 发送结果
     */
    AjaxResult send(EmergencyNotice info);

    /**
     * 删除应急通知
     * @param info 通知信息
     * @return 删除结果
     */
    AjaxResult delete(EmergencyNotice info);
}
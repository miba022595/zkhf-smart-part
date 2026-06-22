package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应急通知实体。
 * 记录应急通知内容、接收对象及发送结果。
 */
@Data
public class EmergencyNotice {
    /**
     * 通知ID
     */
    private String noticeId;
    /**
     * 企业编码
     */
    private String entCode;
    /**
     * 企业名称
     */
    private String entName;
    /**
     * 企业微信通知配置
     */
    private String weComMsg;
    /**
     * 通知标题
     */
    private String noticeTitle;
    /**
     * 通知内容
     */
    private String noticeContent;
    /**
     * 事件地点
     */
    private String eventLocation;
    /**
     * 事件时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;
    /**
     * 接收人姓名列表
     */
    private String receiverNames;
    /**
     * 发送状态：success-成功，failed-失败
     */
    private String sendStatus;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

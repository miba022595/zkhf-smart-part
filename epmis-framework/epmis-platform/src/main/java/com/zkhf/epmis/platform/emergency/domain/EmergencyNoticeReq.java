package com.zkhf.epmis.platform.emergency.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应急通知查询请求对象。
 * 用于通知发送记录筛选，同时承载数据权限范围内的企业编码集合。
 */
@Data
public class EmergencyNoticeReq {
    /**
     * 通知ID
     */
    private String noticeId;
    /**
     * 通知标题关键字
     */
    private String noticeTitle;
    /**
     * 事件地点关键字
     */
    private String eventLocation;
    /**
     * 查询开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 查询结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    /**
     * 指定查询的企业编码
     */
    private String entCode;
    /**
     * 数据权限范围内的企业编码列表
     */
    private List<String> entCodes;
}

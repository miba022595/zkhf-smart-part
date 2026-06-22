package com.zkhf.epmis.platform.approval.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批消息推送：存储需要推送的消息记录，支持多人推送对象 t_bas_approval_message
 */
@Data
public class ApprovalMessage {

    /** 主键ID */
    private Long id;

    /** 关联审批历史ID */
    private Long historyId;

    /** 推送目标用户ID */
    private Long pushUser;
    private String pushUserName;

    /** 消息内容 */
    private String messageContent;

    /** 推送状态：0-未推送，1-已推送，2-推送失败，3-已读（先只要0未读、1已读就行了） */
    private Integer pushStatus;

    /** 推送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    /** 读取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /** 最大重试次数 */
    private Integer maxRetries;

    /** 重试间隔(秒)，默认5分钟 */
    private Integer retryInterval;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}

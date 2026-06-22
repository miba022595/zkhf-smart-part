package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

/**
 * 附件信息（正本/副本下载链接）
 */
@Data
public class Attachment {
    /** 附件名称 */
    private String name;
    /** 下载URL */
    private String url;
    /** 附件类型：正本/副本 */
    private String type;
}
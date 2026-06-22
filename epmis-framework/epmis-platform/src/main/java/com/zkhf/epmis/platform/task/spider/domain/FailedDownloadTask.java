package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 失败下载任务记录（重试与断点续传）
 */
@Data
public class FailedDownloadTask {
    /** 下载URL（正本用）或副本页面URL（副本用） */
    private String url;
    /** 本地文件路径 */
    private String filePath;
    /** 企业名称 */
    private String companyName;
    /** 企业编码（用于注册附件） */
    private String entCode;
    /** 附件类型：正本/副本 */
    private String type;
    /** 已重试次数 */
    private int retryCount = 0;

    // ===== 副本专用字段 =====
    /** 副本图片的pkid */
    private String pkid;
    /** 副本的dataid */
    private String dataid;
    /** 图片总数 */
    private int imgCount;
    /** 失败的页码（从哪一页开始重试） */
    private int failedPageIndex;
    /** 已成功下载的图片路径 */
    private List<String> successImagePaths = new ArrayList<>();
}
package com.zkhf.epmis.core.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnexInfo {

    /**
     * 主键id
     */
    private String annexId;
    /**
     * 附件归属id
     */
    private String sourceId;
    /**
     * 附件归属id的类型
     */
    private String sourceType;
    /**
     * 文件类型:如jpg、txt等
     */
    private String fileType;
    /**
     * 文件的名称，包括扩展名
     */
    private String fileName;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件描述
     */
    private String fileDesc;
    /**
     * 上传人
     */
    private String createUser;
    /**
     * 上传时间
     */
    private LocalDateTime createTime;
}

package com.zkhf.epmis.process.base.domain;

import lombok.Data;

@Data
public class EntInfo {

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 企业负责人姓名
     */
    private String entDirectorName;

    /**
     * 企业负责人电话
     */
    private String entDirectorPhone;

    /**
     * 企业负责人邮箱
     */
    private String entDirectorEmail;
}

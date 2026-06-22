package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

/**
 * 企业摘要信息（API搜索返回的临时数据结构）
 */
@Data
public class CompanySummary {
    /** 许可证编号 */
    private String licenseNumber;
    /** 企业名称 */
    private String companyName;
    /** 行业类别 */
    private String industry;
    /** 有效期起 */
    private String validStart;
    /** 有效期止 */
    private String validEnd;
    /** 发证日期 */
    private String issueDate;
    /** 管理类别 */
    private String managementType;
    /** 数据ID（用于查询详情） */
    private String dataId;
}
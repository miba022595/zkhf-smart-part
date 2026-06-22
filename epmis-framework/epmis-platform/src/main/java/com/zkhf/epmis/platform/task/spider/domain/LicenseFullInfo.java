package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 完整的企业排污许可信息（爬取结果聚合模型）
 */
@Data
public class LicenseFullInfo {
    /** 企业名称 */
    private String companyName;
    /** 是否找到该企业信息 */
    private boolean found;
    /** 许可证编号 */
    private String licenseNo;
    /** 行业类别 */
    private String industry;
    /** 管理类别（重点管理/简化管理） */
    private String managementType;
    /** 有效期起 */
    private String validStart;
    /** 有效期止 */
    private String validEnd;
    /** 发证日期 */
    private String issueDate;
    /** 发证机关 */
    private String issueOrg;
    /** 数据ID（详情页标识） */
    private String dataId;

    /** 主要产品（表格形式） */
    private List<Map<String, String>> mainProducts = new ArrayList<>();
    /** 产量/产能 */
    private String output = "";
    /** 备注 */
    private String remarks = "";
    /** 执行报告报送要求 */
    private String reportRequirements = "";

    /** 排放信息（废气/废水/扬尘/VOC/恶臭/噪声） */
    private Map<String, EmissionCategory> emissionInfo = new LinkedHashMap<>();
    /** 许可排放限值 */
    private List<PermitLimit> permitLimits = new ArrayList<>();
    /** 附件列表 */
    private List<Attachment> attachments = new ArrayList<>();
}
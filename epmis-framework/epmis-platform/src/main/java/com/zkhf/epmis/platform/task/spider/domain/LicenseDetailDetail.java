package com.zkhf.epmis.platform.task.spider.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 详细许可信息（详情页解析的临时数据结构）
 */
@Data
public class LicenseDetailDetail {
    /** 发证机关 */
    private String issueOrg = "";
    /** 主要产品（表格形式，每行为一个产品，列名为产品相关字段） */
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
    /** 附件列表（正本/副本下载链接） */
    private List<Attachment> attachments = new ArrayList<>();
}
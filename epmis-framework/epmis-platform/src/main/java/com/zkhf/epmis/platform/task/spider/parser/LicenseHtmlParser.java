package com.zkhf.epmis.platform.task.spider.parser;

import com.zkhf.epmis.platform.task.spider.domain.Attachment;
import com.zkhf.epmis.platform.task.spider.domain.EmissionCategory;
import com.zkhf.epmis.platform.task.spider.domain.PermitLimit;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 排污许可证页面HTML解析器
 *
 * <p>从平台详情页HTML中提取结构化数据，包含基础信息、产品信息、排放信息、许可限值、附件链接等</p>
 */
@Slf4j
public class LicenseHtmlParser {

    private LicenseHtmlParser() {}

    private static final String BASE_URL = "https://permit.mee.gov.cn";

    // ==================== 综合解析 ====================

    /**
     * 解析页面中所有的"标签:值"对（从 #apply_table）
     */
    public static Map<String, String> parseAllKeyValues(Document doc) {
        Map<String, String> result = new LinkedHashMap<>();

        Element applyTable = doc.select("#apply_table").first();
        if (applyTable != null) {
            Elements tableRows = applyTable.select("tr");
            for (Element row : tableRows) {
                Element th = row.select("th").first();
                Element td = row.select("td").first();

                if (th != null && td != null) {
                    String key = th.text().trim();
                    if (key.endsWith("：") || key.endsWith(":")) {
                        key = key.substring(0, key.length() - 1);
                    }

                    String value = "";
                    Element span = td.select("span").first();
                    if (span != null) {
                        value = span.text().trim();
                    }
                    if (value.isEmpty()) {
                        value = td.text().trim();
                    }

                    if (!key.isEmpty() && !value.isEmpty() && !value.equals("/")) {
                        result.put(key, value);
                        log.debug("解析字段: {} = {}", key, value);
                    }
                }
            }
        }
        // 解析p标签中的发证机关
        Elements pElements = doc.select("p");
        for (Element p : pElements) {
            String text = p.text();
            if (text.contains("发证机关：")) {
                String[] parts = text.split("发证机关：");
                if (parts.length > 1) {
                    String value = parts[1].trim();
                    if (value.contains("&nbsp;")) {
                        value = value.split("&nbsp;")[0];
                    }
                    if (value.contains(" ")) {
                        value = value.split(" ")[0];
                    }
                    result.put("发证机关", value);
                }
            }
        }

        return result;
    }

    // ==================== 发证机关 ====================

    /**
     * 解析发证机关
     */
    public static String parseIssueOrg(Document doc) {
        try {
            Elements pElements = doc.select("p:contains(发证机关)");
            for (Element p : pElements) {
                String text = p.text();
                if (text.contains("发证机关：")) {
                    String[] parts = text.split("发证机关：");
                    if (parts.length > 1) {
                        String issueOrg = parts[1].trim();
                        if (issueOrg.contains(" ")) {
                            issueOrg = issueOrg.split(" ")[0];
                        }
                        if (issueOrg.contains("&nbsp;")) {
                            issueOrg = issueOrg.split("&nbsp;")[0];
                        }
                        return issueOrg;
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析发证机关失败", e);
        }
        return "";
    }

    // ==================== 产品信息 ====================

    /**
     * 解析主要产品
     */
    public static List<Map<String, String>> parseMainProducts(Document doc, Map<String, String> allKeyValues) {
        List<Map<String, String>> products = new ArrayList<>();

        Element productTable = findTableByKeyword(doc, Arrays.asList("主要产品", "产品", "产品名称"));
        if (productTable != null) {
            products.addAll(parseTableToMapList(productTable));
        }

        if (products.isEmpty()) {
            for (Map.Entry<String, String> entry : allKeyValues.entrySet()) {
                if (entry.getKey().contains("产品") && !entry.getValue().isEmpty()) {
                    Map<String, String> product = new LinkedHashMap<>();
                    product.put("productName", entry.getKey());
                    product.put("description", entry.getValue());
                    products.add(product);
                }
            }
        }

        return products;
    }

    /**
     * 解析产量
     */
    public static String parseOutput(Map<String, String> allKeyValues) {
        for (Map.Entry<String, String> entry : allKeyValues.entrySet()) {
            if (entry.getKey().contains("产量") || entry.getKey().contains("产能")) {
                return entry.getValue();
            }
        }
        return "";
    }

    /**
     * 解析备注
     */
    public static String parseRemarks(Map<String, String> allKeyValues) {
        for (Map.Entry<String, String> entry : allKeyValues.entrySet()) {
            if (entry.getKey().contains("备注")) {
                return entry.getValue();
            }
        }
        return "";
    }

    // ==================== 执行报告要求 ====================

    /**
     * 解析执行报告报送要求
     */
    public static String parseReportRequirements(Document doc, Map<String, String> allKeyValues) {
        for (Map.Entry<String, String> entry : allKeyValues.entrySet()) {
            if (entry.getKey().contains("执行报告") || entry.getKey().contains("报告要求")) {
                return entry.getValue();
            }
        }

        Element reportTable = findTableByKeyword(doc, Arrays.asList("执行报告", "年度执行报告", "季度执行报告"));
        if (reportTable != null) {
            return reportTable.text();
        }

        return "";
    }

    // ==================== 排放信息 ====================

    /**
     * 解析排放信息（废气、废水、扬尘、VOC、恶臭、噪声）
     */
    public static Map<String, EmissionCategory> parseEmissionInfo(Map<String, String> allKeyValues) {
        Map<String, EmissionCategory> emissionInfo = new LinkedHashMap<>();

        // 1. 废气
        EmissionCategory wasteGas = buildEmissionCategory("废气", allKeyValues,
                Arrays.asList("大气主要污染物种类", "废气污染物种类", "大气污染物种类", "无组织排放污染物种类", "厂界污染物种类"),
                Arrays.asList("大气污染物排放规律", "废气排放规律", "无组织排放规律"),
                Arrays.asList("大气污染物排放执行标准", "废气排放标准", "无组织排放执行标准", "厂界排放标准"));
        if (hasContent(wasteGas)) emissionInfo.put("废气", wasteGas);

        // 2. 废水
        EmissionCategory wasteWater = buildEmissionCategory("废水", allKeyValues,
                Arrays.asList("废水主要污染物种类", "水污染物种类", "废水污染物种类"),
                Collections.singletonList("废水污染物排放规律"),
                Arrays.asList("废水污染物排放执行标准", "水污染物排放标准"));
        if (hasContent(wasteWater)) emissionInfo.put("废水", wasteWater);

        // 3. 扬尘
        EmissionCategory dust = buildEmissionCategory("扬尘", allKeyValues,
                Arrays.asList("扬尘污染物种类", "粉尘污染物种类", "颗粒物种类", "烟尘种类"),
                Arrays.asList("扬尘排放规律", "粉尘排放规律"),
                Arrays.asList("扬尘排放标准", "粉尘排放标准", "颗粒物排放标准"));
        if (hasContent(dust)) emissionInfo.put("扬尘", dust);

        // 4. VOC
        EmissionCategory voc = buildEmissionCategory("VOC", allKeyValues,
                Arrays.asList("VOCs种类", "挥发性有机物种类"),
                Collections.emptyList(),
                Arrays.asList("VOCs排放标准", "挥发性有机物排放标准"));
        if (hasContent(voc)) emissionInfo.put("VOC", voc);

        // 5. 恶臭
        EmissionCategory odor = buildEmissionCategory("恶臭", allKeyValues,
                Arrays.asList("恶臭污染物种类", "异味污染物种类", "臭气种类"),
                Collections.singletonList("恶臭排放规律"),
                Arrays.asList("恶臭污染物排放标准", "恶臭排放标准", "GB 14554"));
        if (hasContent(odor)) emissionInfo.put("恶臭", odor);

        // 6. 噪声
        EmissionCategory noise = buildEmissionCategory("噪声", allKeyValues,
                Arrays.asList("噪声源种类", "噪声污染物种类", "噪声类型"),
                Arrays.asList("噪声排放规律", "噪声产生规律"),
                Arrays.asList("噪声排放标准", "工业企业厂界环境噪声排放标准", "GB 12348", "GB 3096"));
        if (hasContent(noise)) emissionInfo.put("噪声", noise);

        return emissionInfo;
    }

    private static EmissionCategory buildEmissionCategory(String type, Map<String, String> allKeyValues,
                                                           List<String> pollutantLabels, List<String> patternLabels, List<String> standardLabels) {
        EmissionCategory cat = new EmissionCategory();
        cat.setType(type);
        cat.setPollutants(getValueByLabels(allKeyValues, pollutantLabels));
        if (!patternLabels.isEmpty()) {
            cat.setEmissionPattern(getValueByLabels(allKeyValues, patternLabels));
        }
        cat.setExecStandard(getValueByLabels(allKeyValues, standardLabels));
        return cat;
    }

    /**
     * 从标签值对中获取指定标签的值（清理标签前缀）
     */
    public static String getValueByLabels(Map<String, String> allKeyValues, List<String> labels) {
        for (String label : labels) {
            for (Map.Entry<String, String> entry : allKeyValues.entrySet()) {
                if (entry.getKey().contains(label)) {
                    String value = entry.getValue();
                    if (value == null || value.isEmpty() || "/".equals(value)) {
                        return "";
                    }
                    for (String lbl : labels) {
                        if (value.contains(lbl + "：")) {
                            value = value.substring(value.indexOf(lbl + "：") + (lbl + "：").length());
                        }
                        if (value.contains(lbl + ":")) {
                            value = value.substring(value.indexOf(lbl + ":") + (lbl + ":").length());
                        }
                    }
                    String[] otherLabels = {"大气污染物排放执行标准", "废水污染物排放执行标准",
                            "大气污染物排放规律", "废水污染物排放规律", "主要产品", "产量", "备注"};
                    for (String other : otherLabels) {
                        if (value.contains(other)) {
                            value = value.substring(0, value.indexOf(other)).trim();
                            break;
                        }
                    }
                    String trimmedValue = value.trim();
                    if (trimmedValue.contains("排放规律") || trimmedValue.contains("排放标准")) {
                        return "";
                    }
                    return trimmedValue;
                }
            }
        }
        return "";
    }

    /**
     * 判断排放类别是否有有效内容
     */
    public static boolean hasContent(EmissionCategory category) {
        return (category.getPollutants() != null && !category.getPollutants().isEmpty()) ||
                (category.getEmissionPattern() != null && !category.getEmissionPattern().isEmpty()) ||
                (category.getExecStandard() != null && !category.getExecStandard().isEmpty());
    }

    // ==================== 许可限值 ====================

    /**
     * 解析许可限值
     */
    public static List<PermitLimit> parsePermitLimits(Document doc) {
        List<PermitLimit> limits = new ArrayList<>();
        Element limitTable = findTableByKeyword(doc, Arrays.asList("许可排放浓度", "许可排放量", "排放限值", "污染物排放限值"));
        if (limitTable != null) {
            limits.addAll(parseLimitTable(limitTable));
        }
        return limits;
    }

    private static List<PermitLimit> parseLimitTable(Element table) {
        List<PermitLimit> limits = new ArrayList<>();
        Elements rows = table.select("tr");
        if (rows.isEmpty()) return limits;

        List<String> headers = new ArrayList<>();
        Element headerRow = rows.get(0);
        for (Element th : headerRow.select("th")) headers.add(th.text().trim());
        for (Element td : headerRow.select("td")) headers.add(td.text().trim());

        for (int i = 1; i < rows.size(); i++) {
            Elements tds = rows.get(i).select("td");
            if (tds.isEmpty()) continue;

            PermitLimit limit = new PermitLimit();
            for (int j = 0; j < tds.size() && j < headers.size(); j++) {
                String header = headers.get(j);
                String value = tds.get(j).text().trim();

                if (header.contains("污染物") || header.contains("种类")) {
                    limit.setPollutant(value);
                } else if (header.contains("浓度")) {
                    limit.setConcentrationLimit(value);
                } else if (header.contains("排放量") || header.contains("总量")) {
                    limit.setTotalLimit(value);
                } else if (header.contains("排口") || header.contains("排放口")) {
                    limit.setOutlet(value);
                } else if (header.contains("标准")) {
                    limit.setStandard(value);
                }
            }
            if (limit.getPollutant() != null && !limit.getPollutant().isEmpty()) {
                limits.add(limit);
            }
        }
        return limits;
    }

    // ==================== 附件链接 ====================

    /**
     * 解析附件链接（正本、副本）
     */
    public static List<Attachment> parseAttachments(Document doc, String dataId) {
        List<Attachment> attachments = new ArrayList<>();

        // 正本
        Element originalLink = doc.select("a:contains(排污许可证正本)").first();
        if (originalLink == null) {
            originalLink = doc.select("a:contains(正本)").first();
        }
        if (originalLink != null) {
            String href = originalLink.attr("href");
            if (href != null && !href.isEmpty()) {
                if (href.startsWith("/")) href = BASE_URL + href;
                Attachment att = new Attachment();
                att.setType("正本");
                att.setName("排污许可证正本");
                att.setUrl(href);
                attachments.add(att);
                log.debug("找到正本链接: {}", href);
            }
        }

        // 副本
        Element copyLink = doc.select("a:contains(排污许可证副本)").first();
        if (copyLink == null) {
            copyLink = doc.select("a:contains(副本)").first();
        }
        if (copyLink != null) {
            String href = copyLink.attr("href");
            if (href != null && !href.isEmpty()) {
                if (href.startsWith("/")) href = BASE_URL + href;
                Attachment att = new Attachment();
                att.setType("副本");
                att.setName("排污许可证副本");
                att.setUrl(href);
                attachments.add(att);
                log.debug("找到副本链接: {}", href);
            }
        } else {
            String copyUrl = BASE_URL + "/perxxgkinfo/syssb/wysb/hpsp/hpsp-company-sewage!showImage.action?dataid=" + dataId;
            Attachment att = new Attachment();
            att.setType("副本");
            att.setName("排污许可证副本");
            att.setUrl(copyUrl);
            attachments.add(att);
            log.debug("dataid构造副本链接: {}", copyUrl);
        }

        return attachments;
    }

    // ==================== 表格查询辅助方法 ====================

    /**
     * 根据关键词查找表格
     */
    public static Element findTableByKeyword(Document doc, List<String> keywords) {
        for (String keyword : keywords) {
            Elements elems = doc.select(":matchesOwn(" + Pattern.quote(keyword) + ")");
            for (Element elem : elems) {
                Element table = elem.closest("table");
                if (table != null) return table;
                Element parent = elem.parent();
                while (parent != null) {
                    table = parent.selectFirst("table");
                    if (table != null) return table;
                    parent = parent.parent();
                }
            }
        }
        return null;
    }

    /**
     * 解析表格为Map列表
     */
    public static List<Map<String, String>> parseTableToMapList(Element table) {
        List<Map<String, String>> results = new ArrayList<>();
        Elements rows = table.select("tr");
        if (rows.isEmpty()) return results;

        List<String> headers = new ArrayList<>();
        Element headerRow = rows.get(0);
        for (Element th : headerRow.select("th")) headers.add(th.text().trim());
        for (Element td : headerRow.select("td")) headers.add(td.text().trim());

        if (headers.isEmpty()) return results;

        for (int i = 1; i < rows.size(); i++) {
            Elements tds = rows.get(i).select("td");
            if (tds.size() != headers.size()) continue;

            Map<String, String> rowData = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                rowData.put(headers.get(j), tds.get(j).text().trim());
            }
            results.add(rowData);
        }

        return results;
    }
}
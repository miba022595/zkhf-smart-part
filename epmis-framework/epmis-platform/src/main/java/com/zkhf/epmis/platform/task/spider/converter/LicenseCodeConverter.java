package com.zkhf.epmis.platform.task.spider.converter;

import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.PollutantCode;
import com.zkhf.epmis.platform.base.service.DictService;
import com.zkhf.epmis.platform.base.service.PollutantCodeService;
import com.zkhf.epmis.platform.task.spider.domain.EmissionCategory;
import com.zkhf.epmis.platform.task.spider.domain.LicenseFullInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 爬取中文名称 → 平台内部编码 转换器
 *
 * <p>转换优先级：数据库字典/污染物表 → YAML手动映射兜底 → 无法转换则排除不写入</p>
 * <p>字典和污染物码表数据仅在首次使用时加载一次，后续复用缓存</p>
 */
@Slf4j
@Component
public class LicenseCodeConverter {

    private DictService dictService;
    @Autowired
    public void setDictService(DictService dictService) {
        this.dictService = dictService;
    }

    private PollutantCodeService pollutantCodeService;
    @Autowired
    public void setPollutantCodeService(PollutantCodeService pollutantCodeService) {
        this.pollutantCodeService = pollutantCodeService;
    }

    @Value("#{${license-spider.pollutant-mapping:{}}}")
    private Map<String, String> pollutantManualMapping;

    @Value("#{${license-spider.dict-mapping:{}}}")
    private Map<String, String> dictManualMapping;

    /** 字典 label→value，全局缓存 */
    private volatile Map<String, String> labelToValue;
    /** 污染物中文名→编码，全局缓存 */
    private volatile Map<String, String> nameToCode;

    public void initCache() {
        loadDictCache();
        loadPollutantCache();
    }

    private synchronized void loadDictCache() {
        labelToValue = new HashMap<>();
        List<String> dictTypes = Arrays.asList("poll_permit_cate", "emission_rule_gas", "emission_rule_water");
        List<DictData> dictList = dictService.getDataListByTypes(dictTypes);
        if (dictList != null) {
            for (DictData d : dictList) {
                labelToValue.put(d.getDictLabel(), d.getDictValue());
            }
        }
        log.debug("字典缓存加载完成: {} 条", labelToValue.size());
    }

    private synchronized void loadPollutantCache() {
        nameToCode = new HashMap<>();
        Map<String, PollutantCode> codeMap = pollutantCodeService.selectAllPollCodeMap();
        if (codeMap != null) {
            for (PollutantCode pc : codeMap.values()) {
                if (StringUtils.isNotEmpty(pc.getPollutantNameCn())) {
                    nameToCode.put(pc.getPollutantNameCn(), pc.getPollutantCode());
                }
            }
        }
        // 合并 YAML 手动映射（优先级低于数据库，不覆盖已有）
        if (pollutantManualMapping != null) {
            for (Map.Entry<String, String> entry : pollutantManualMapping.entrySet()) {
                nameToCode.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        log.debug("污染物缓存加载完成: {} 条", nameToCode.size());
    }

    /**
     * 将爬取到的中文名称转换为系统内部编码
     *
     * @param info 完整许可信息，转换后直接修改其字段值
     */
    public void convertChineseNameToCode(LicenseFullInfo info) {
        String companyName = info.getCompanyName();

        // 1. 管理类别 DB → YAML fallback → 日志
        String mgmtLabel = info.getManagementType();
        if (mgmtLabel != null && !mgmtLabel.isEmpty()) {
            String mgmtCode = labelToValue.get(mgmtLabel);
            if (mgmtCode == null && dictManualMapping != null) {
                mgmtCode = dictManualMapping.get(mgmtLabel);
            }
            if (mgmtCode != null) {
                info.setManagementType(mgmtCode);
            } else {
                log.warn("[{}] 管理类别编码未匹配，设为空: {}", companyName, mgmtLabel);
                info.setManagementType(null);
            }
        }

        // 2. 废气/废水排放规律 DB → YAML fallback → 日志
        if (info.getEmissionInfo() != null) {
            for (Map.Entry<String, EmissionCategory> entry : info.getEmissionInfo().entrySet()) {
                String catName = entry.getKey();
                EmissionCategory cat = entry.getValue();
                String ruleLabel = cat.getEmissionPattern();
                if (ruleLabel != null && !ruleLabel.isEmpty()) {
                    String ruleCode = labelToValue.get(ruleLabel);
                    if (ruleCode == null && dictManualMapping != null) {
                        ruleCode = dictManualMapping.get(ruleLabel);
                    }
                    if (ruleCode != null) {
                        cat.setEmissionPattern(ruleCode);
                    } else {
                        log.warn("[{}] {}排放规律编码未匹配，设为空: {}", companyName, catName, ruleLabel);
                        cat.setEmissionPattern(null);
                    }
                }
            }
        }

        // 3. 污染物种类：能转换的才写入，不能的排除
        if (info.getEmissionInfo() != null) {
            for (EmissionCategory cat : info.getEmissionInfo().values()) {
                String raw = cat.getPollutants();
                if (raw != null && !raw.isEmpty()) {
                    cat.setPollutants(convertPollNames(raw, companyName));
                }
            }
        }
    }

    /**
     * 将逗号分隔的中文污染物名转换为编码串，
     * 无法转换的排除不写入，仅记录日志。
     *
     * @param raw         原始中文名串，如 "二氧化硫,氮氧化物"
     * @param companyName 企业名称（用于日志）
     * @return 编码串，如 "SO2,NOx"；全部无法转换则返回空串
     */
    public String convertPollNames(String raw, String companyName) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String name : raw.split(",")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String code = nameToCode.get(trimmed);
            if (code != null) {
                // 精确匹配成功
                if (!sb.isEmpty()) {
                    sb.append(",");
                }
                sb.append(code);
                continue;
            }
            // 尝试提取括号前的内容（如：氨氮（NH3-N） -> 氨氮）
            String beforeBracket = extractBeforeBracket(trimmed);
            log.debug("匹配括号前信息 {} {}", trimmed, beforeBracket);
            if (beforeBracket != null && !beforeBracket.equals(trimmed)) {
                code = nameToCode.get(beforeBracket);
            }
            // 如果还匹配不上，尝试提取括号内的内容（如：氨（氨气） -> 氨气）
            if (code == null) {
                String insideBracket = extractInsideBracket(trimmed);
                log.debug("匹配括号中信息 {} {}", trimmed, insideBracket);
                if (insideBracket != null) {
                    code = nameToCode.get(insideBracket);
                }
            }
            if (code != null) {
                if (!sb.isEmpty()) {
                    sb.append(",");
                }
                sb.append(code);
            } else {
                log.warn("[{}] 污染物编码未匹配，已排除: {}", companyName, trimmed);
            }
        }
        return sb.toString();
    }

    /**
     * 提取括号前的内容
     * 示例：氨氮（NH3-N） -> 氨氮
     */
    private String extractBeforeBracket(String str) {
        int idx = str.indexOf("（");
        if (idx == -1) {
            idx = str.indexOf("(");
        }
        if (idx > 0) {
            return str.substring(0, idx).trim();
        }
        return null;
    }

    /**
     * 提取括号内的内容
     * 示例：氨（氨气） -> 氨气
     */
    private String extractInsideBracket(String str) {
        int start = str.indexOf("（"), end;
        if (start > -1) {
            end = str.indexOf("）");
            if (end > -1 && end > start + 1) {
                return str.substring(start + 1, end).trim();
            }
            return null;
        }
        start = str.indexOf("(");
        end = str.indexOf(")");
        if (end > -1 && start > -1 && end > start + 1) {
            return str.substring(start + 1, end).trim();
        }
        return null;
    }
}
package com.zkhf.epmis.platform.envProtect.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 企业清洁生产基础对象 t_ent_clean_produce
 */
@Data
public class EntCleanProduce {

    /**
     * 排污许可id
     */
    private String cleanProduceId;

    /**
     * 企业编码
     */
    private String entCode;
    /**
     * 企业名称
     */
    private String entName;

    /**
     * 名称
     */
    private String cleanName;

    /**
     * 编制时间
     */
    private LocalDate makeDate;

    /**
     * 审核重点
     */
    private String auditFocus;

    /**
     * 方案情况
     */
    private String planInfo;

    /**
     * 减排效果
     */
    private String reduceEffect;

    /**
     * 工作进展
     */
    private String workProgress;

    /**
     * 实施时间
     */
    private LocalDate effectiveDate;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
    /**
     * 附件列表（查询时用）
     */
    private List<AnnexInfo> annexInfoList;
}

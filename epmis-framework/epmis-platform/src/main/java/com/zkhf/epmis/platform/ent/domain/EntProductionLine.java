package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 企业生产线信息对象 t_ent_production_line
 */
@Data
public class EntProductionLine {
    /** 设备状态：1-在用，2-停用 */
    public static final Integer status_ON = 1;
    public static final Integer status_OFF = 2;

    /** 生产线ID */
    private String lineId;

    /** 生产线编码，车间下的编码唯一 */
    private String lineCode;

    /** 生产线名称 */
    private String lineName;

    /** 所属车间ID, 必填 */
    private String workshopId;

    /** 所属企业 */
    private String entCode;
    private String entName;

    /** 生产车间 */
    private String workshopCode;
    private String workshopName;

    /** 工艺类型 */
    private String processType;

    /** 产品类型 */
    private String productType;

    /** 状态：{@link com.zkhf.epmis.core.enums.PublicStatusEnum}*/
    private Integer status;

    /** 设计产能 */
    private BigDecimal capacity;

    /** 产能单位 */
    private String capacityUnit;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 生产设施用电附件ID列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> productionFacilityAnnexIds;

    /**
     * 生产设施用电图附件列表（查询时使用）
     */
    private List<AnnexInfo> productionFacilityAnnexList;

    /**
     * 治理设施用电图附件ID列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> governanceFacilityAnnexIds;

    /**
     * 治理设施用电图附件列表（查询时使用）
     */
    private List<AnnexInfo> governanceFacilityAnnexList;

    /**
     * 关联生产设施id列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> produceFacilityIds;

    /**
     * 关联生产设施信息
     */
    private List<Map<String, Object>> produceFacilityList;

    /**
     * 关联污染治理设施id列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> governanceFacilityIds;

    /**
     * 关联污染治理设施信息
     */
    private List<Map<String, Object>> governanceFacilityList;

    /**
     * 关联排口的id列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> outPutIdList;

    /**
     * 关联排口信息
     */
    private List<Map<String, Object>> outPutList;
}

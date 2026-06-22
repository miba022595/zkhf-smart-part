package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 企业生产车间对象 t_ent_produce_workshop
 */
@Data
public class EntProduceWorkshop {

    /** 企业生产车间主键id */
    private String workshopId;

    /** 所属企业 */
    private String entCode;
    private String entName;

    /** 生产车间名称 */
    private String workshopName;

    /** 生产车间编号 */
    private String workshopCode;

    /** 归属管理人员ID */
    private Long perId;
    private String perName;
    private String perPhone;
    private String perEmail;
    private String perNickName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 车间图纸等附件（更新时用） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 关联生产设施id列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> produceFacilityIds;

    /**
     * 关联生产设施信息
     */
    private List<Map<String, Object>> relateProduceFacilityList;

    /**
     * 关联污染治理设施id列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> pollControlFacilityIds;

    /**
     * 关联污染治理设施信息
     */
    private List<Map<String, Object>> relatePollControlFacilityList;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;
}
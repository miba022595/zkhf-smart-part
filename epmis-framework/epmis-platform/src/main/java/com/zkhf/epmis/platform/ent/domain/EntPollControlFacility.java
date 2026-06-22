package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 企业污染治理设施对象 t_ent_poll_control_facility
 */
@Data
public class EntPollControlFacility {

    /**
     * 主键id
     */
    private String facilityId;

    /**
     * 关联企业id
     */
    private String entCode;
    private String entName;

    /**
     * 排放口类型 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 治理设施编号
     */
    private String facilityCode;

    /**
     * 治理设施名称
     */
    private String facilityName;

    /**
     * 主要污染物
     */
    private String pollutant;

    /**
     * 治理工艺
     */
    private String governProcess;

    /**
     * 设计治理效率(0-100%)
     */
    private Integer efficiency;

    /**
     * 设计处理能力
     */
    private String designCapacity;

    /**
     * 设计治理设施运行率(0-100%)
     */
    private Integer actualOperatingRate;

    /**
     * 安装时间
     */
    private LocalDate installDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 关联排口信息（更新时用）
     */
    private List<Map<String, Object>> relateOutPutList;
}

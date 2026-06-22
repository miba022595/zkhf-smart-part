package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运维记录对象 t_ops_record
 */
@Data
public class OpsRecord {

    /** 运维记录ID（主键） */
    private String recordId;

    /** 企业编码 */
    private String entCode;
    private String entName;
    private String region;
    private String regionDesc;

    /** 排放口ID */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 关联的运维类型 */
    private String templateCode;
    private String templateName;

    /** 运维企业编码-第三方单位 */
    private String opsUnitId;
    private String opsUnitName;

    /** 运维时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordDate;

    /** 运维人员 */
    private Long opsUserId;
    private String opsUserName;
    private String opsNickName;

    /** 是否合格（1：合格，0：不合格） */
    private Integer qualifiedFlag;

    /** 审核状态 {@link com.zkhf.epmis.core.enums.GenApprovalType}  */
    private Integer reviewStatus;
    private String reviewStatusDesc;

    /** 运维记录项 */
    @JsonIgnore
    private String recordArray;
    private List<OpsRecordItem> recordList;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 附件列表（更新时用），附件类型 opsRecord
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 附件列表（查询时用）
     */
    private List<AnnexInfo> annexInfoList;

}

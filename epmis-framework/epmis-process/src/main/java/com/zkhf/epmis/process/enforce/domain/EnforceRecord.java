package com.zkhf.epmis.process.enforce.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 执法检查记录对象 t_enforce_record
 */
@Data
public class EnforceRecord {

    /** 执法记录ID */
    private Long id;

    /** 企业编码 */
    private String entCode;
    private String entName;

    /** 排放口ID */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 检查单位名称 */
    private String unitName;

    /** 检查人，多个逗号拼接，页面输入 */
    private String inspector;

    /** 运维单位ID（第三方单位） */
    private String opsUnit;
    private String opsUnitName;

    /** 运维人员ID */
    private Long opsUser;
    private String opsUserName;

    /** 检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkDate;

    /** 检查原因 */
    private String checkReason;

    /** 检查结果是否合格（1：合格，0：不合格/异常） */
    private Integer checkFlag;

    /** 检查结论 */
    private String checkConclusion;

    /** 备注 */
    private String remark;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

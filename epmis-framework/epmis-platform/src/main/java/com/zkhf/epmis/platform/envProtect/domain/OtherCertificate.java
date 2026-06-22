package com.zkhf.epmis.platform.envProtect.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 其他证书对象 t_other_certificate
 */
@Data
public class OtherCertificate {

    /**
     * 其他证书主键id
     */
    private String otherId;

    /**
     * 添加证书的企业
     */
    private String entCode;
    private String entName;

    /**
     * 证书名称
     */
    private String certName;

    /**
     * 发证机构
     */
    private String issueOffice;

    /**
     * 归属
     */
    private String certBelong;

    /**
     * 归属类型，1机构，2个人
     */
    private Integer belongType;

    /**
     * 有效期-开始时间
     */
    private LocalDate beginDate;

    /**
     * 有效期-结束时间
     */
    private LocalDate endDate;

    /**
     * 发证日期
     */
    private LocalDate issueDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

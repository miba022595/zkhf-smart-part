package com.zkhf.epmis.platform.envProtect.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 企业环保人员对象 t_env_pro_person
 */
@Data
public class EnvProPerson {

    /**
     * 环保人员主键id
     */
    private String proPersonId;

    /**
     * 企业编码
     */
    private String entCode;
    /**
     * 企业名称
     */
    private String entName;

    /**
     * 环保人员姓名
     */
    private String proName;

    /**
     * 环保人员编号
     */
    private String proCode;

    /**
     * 性别，0男，1女
     */
    private Integer proSex;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 联系电话
     */
    private String telPhone;

    /**
     * 职称/证书
     */
    private String proTitle;

    /**
     * 岗位
     */
    private String proPost;

    /**
     * 住址
     */
    private String address;

    /**
     * 入职时间
     */
    private LocalDate entryDate;

    /**
     * 离职时间
     */
    private LocalDate resignDate;

    /**
     * 通过入职、离职时间和当前时间判断在职状态，0离职，1在职，2未入职
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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

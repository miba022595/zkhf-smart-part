package com.zkhf.epmis.platform.envProtect.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业排污许可基础对象 t_bas_ent_out_pollutant_permit
 */
@Data
public class EntOutPollutantPermit {

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 社会信用代码
     */
    private String socialCreditCode;

    /**
     * 排污许可管理类别
     */
    private String permitLevel;
    private String permitLevelDesc;

    /**
     * 许可证编号
     */
    private String permitNum;

    /**
     * 有效期-开始时间（yyyy-MM-dd）
     */
    private String beginDate;

    /**
     * 有效期-结束时间（yyyy-MM-dd）
     */
    private String endDate;

    /**
     * 发证机关
     */
    private String issueOffice;

    /**
     * 发证日期（yyyy-MM-dd）
     */
    private String issueDate;

    /**
     * 执行报告报送要求
     */
    private String reportRequire;

    /**
     * 主要产品
     */
    private String productDesc;

    /**
     * 产量
     */
    private String productOutput;

    /**
     * 废气污染物种类(pollutantCodes)
     */
    private String gasPollType;
    private String gasPollDesc;
    private String gasPollUnit;

    /**
     * 废气排放规律
     */
    private String gasEmissionRule;
    private String gasEmissionRuleDesc;

    /**
     * 废气执行标准
     */
    private String gasExecuteStandard;

    /**
     * 废气特征污染物
     */
    private String gasCharPoll;

    /**
     * 废水污染物种类(pollutantCodes)
     */
    private String waterPollType;
    private String waterPollDesc;
    private String waterPollUnit;

    /**
     * 废水排放规律
     */
    private String waterEmissionRule;
    private String waterEmissionRuleDesc;

    /**
     * 废水执行标准
     */
    private String waterExecuteStandard;

    /**
     * 废水特征污染物
     */
    private String waterCharPoll;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 附件主键id
     */
    private List<String> annexIds;

    /**
     * 附件列表（查询时返回）
     */
    private List<AnnexInfo> annexInfoList;
}

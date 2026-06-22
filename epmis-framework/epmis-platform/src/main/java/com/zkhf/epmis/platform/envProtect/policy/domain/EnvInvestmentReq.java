package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

import java.util.List;

/**
 * 环保法规与体系管理-环保投入对象 t_env_investment
 */
@Data
public class EnvInvestmentReq {

    /** 当前页的第一条，取上一页用 */
    private String investmentIdF;

    /** 当前页的最后一条，取下一页用 */
    private String investmentIdE;

    /** 所属企业-权限 */
    private String entCode;
    private List<String> entCodes;

    /** 所属企业-admin下模糊匹配 */
    private String entName;

    /** 项目名称-模糊 */
    private String projectName;

    /** 是否取得政府资金支持，1是，其他否 */
    private Integer governmentFund;

    private Integer pageSize;

    /** 跳页时使用，偏移量获取 */
    private Long offset;
}

package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

import java.util.List;

/**
 * 政府资金支持对象 t_govern_fund_support
 */
@Data
public class GovernFundSupportReq {

    /**
     * 企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 企业名称-模糊，admin使用
     */
    private String entName;

    /**
     * 项目名称
     */
    private String projectName;
}

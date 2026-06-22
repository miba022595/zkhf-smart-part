package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

import java.util.List;

/**
 * 环境政策法规信息 t_env_policy_regulation
 */
@Data
public class EnvPolicyReq {

    /**
     * 所属企业-权限
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 所属企业-admin下模糊匹配
     */
    private String entName;

    /**
     * 政策法规名称-模糊
     */
    private String policyName;

    /**
     * 法规类型，字典regulatory_type
     */
    private String policyType;

    /**
     * 法规状态，字典regulatory_status
     */
    private Integer status;

    /**
     * 公共标识：null不限制，-1查公共的法规文件，1则查私有的文件
     */
    private String pubSign;
}

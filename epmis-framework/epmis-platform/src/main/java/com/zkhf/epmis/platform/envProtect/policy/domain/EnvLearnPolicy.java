package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

/**
 * 环境政策法规学习-政策法规关联表 t_env_learn_policy
 */
@Data
public class EnvLearnPolicy {

    /**
     * 学习id
     */
    private String learnId;

    /**
     * 政策法规Id
     */
    private String policyId;
    private String policyName;
}
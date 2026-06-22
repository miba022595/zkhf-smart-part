package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

/**
 * 环境政策法规学习-企业关联表 t_env_learn_ent
 */
@Data
public class EnvLearnEnt {

    /**
     * 学习id
     */
    private String learnId;

    /**
     * 企业code
     */
    private String entCode;
    private String entName;
}
package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 企业环评环保管理-项目和环评、环保验收的关联表 t_env_mange_relate
 */
@Data
@Builder
public class EnvMangeRelate {
    /** 关联关系类型：环评 */
    public static final Integer RELATE_HP = 1;
    /** 关联关系类型：环保验收 */
    public static final Integer RELATE_YS = 2;

    /**
     * 环评环保管理-项目主键id
     */
    private String projectId;

    /**
     * 关联类型，1环评、2环保验收
     */
    private Integer relateType;

    /**
     * 环评环保管理-关联主键id
     */
    private String relateId;
}

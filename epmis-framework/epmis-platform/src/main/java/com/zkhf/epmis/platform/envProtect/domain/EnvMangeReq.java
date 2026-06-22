package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业环评环保管理-项目对象 t_env_mange_project
 */
@Data
public class EnvMangeReq {

    /**
     * 查询字段，用作项目名称、获取环评时的项目主键id
     */
    private String key;

    /**
     * 企业筛选，模糊
     */
    private String entName;

    /**
     * 权限管理
     */
    private String entCode;
    private List<String> entCodes;
}

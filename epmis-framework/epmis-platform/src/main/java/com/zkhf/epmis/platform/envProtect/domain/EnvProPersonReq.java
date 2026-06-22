package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 企业环保人员对象 t_env_pro_person
 */
@Data
public class EnvProPersonReq {

    /**
     * 权限管理
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 企业名称（模糊）
     */
    private String entName;

    /**
     * 环保人员姓名（模糊）
     */
    private String proName;

    /**
     * 性别
     */
    private Integer proSex;

    /**
     * 岗位
     */
    private String proPost;

    /**
     * 通过入职、离职时间和当前时间判断在职状态，1在职，0离职
     */
    private Integer status;

    /**
     * 当前时间
     */
    private LocalDate now = LocalDate.now();
}

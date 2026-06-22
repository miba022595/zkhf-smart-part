package com.zkhf.epmis.auth.core.domain.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 企业基础对象 t_bas_enterprise
 */
@Data
@Builder
public class SysEntTree {

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 社会统一信用代码 */
    private String socialCreditCode;

    /** 父节点编码 */
    private String parentCode;

    /** 子节点 */
    private List<SysEntTree> subList;

    /** 是否由当前节点的权限 */
    private boolean auth;
}

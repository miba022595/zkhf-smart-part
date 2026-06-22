package com.zkhf.epmis.platform.base.domain.entity;

import lombok.Data;

/**
 * 企业基础对象 t_bas_enterprise
 */
@Data
public class SysEnt {

    /** 企业编码 */
    private String entCode;

    /** 父节点编码 */
    private String parentCode;

    /** 企业名称 */
    private String entName;

    /** 社会统一信用代码 */
    private String socialCreditCode;
}

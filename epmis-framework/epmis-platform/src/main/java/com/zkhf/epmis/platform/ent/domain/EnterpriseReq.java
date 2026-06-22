package com.zkhf.epmis.platform.ent.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业基础对象 t_bas_enterprise
 */
@Data
public class EnterpriseReq {

    /**
     * 企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 企业名称-模糊
     */
    private String entName;

    /**
     * 企业状态
     */
    private String entStatus;
}

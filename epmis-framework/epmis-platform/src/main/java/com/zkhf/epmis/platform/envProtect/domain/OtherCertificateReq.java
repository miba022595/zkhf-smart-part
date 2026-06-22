package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

import java.util.List;

/**
 * 其他证书对象 t_other_certificate
 */
@Data
public class OtherCertificateReq {

    /**
     * 证书名称（模糊）
     */
    private String certName;

    /**
     * 发证机构（模糊）
     */
    private String issueOffice;

    /**
     * 归属（模糊）
     */
    private String certBelong;

    /**
     * 归属类型，1机构，2个人
     */
    private Integer belongType;

    /**
     * 权限管理
     */
    private String entCode;
    private List<String> entCodes;
}

package com.zkhf.epmis.platform.ent.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EntTree {

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 上级企业编码
     */
    private String parentCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 社会统一信用代码
     */
    private String socialCreditCode;

    /**
     * 企业简称
     */
    private String shorterName;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /** 子节点 */
    private List<Object> subList;
}

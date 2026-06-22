package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/**
 * 企业基础部分对象 t_bas_enterprise
 */
@Data
public class EnterprisePart {

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

    /**
     * 所在地区（地区选择）
     * id1,id2...
     */
    @JsonIgnore
    private String region;

    /**
     * 排口列表信息
     */
    private List<EntOutPutPart> outList;

    /**
     * 企业负责人姓名
     */
    private String entDirectorName;

    /**
     * 企业负责人电话
     */
    private String entDirectorPhone;

    /**
     * 企业负责人邮箱
     */
    private String entDirectorEmail;
}

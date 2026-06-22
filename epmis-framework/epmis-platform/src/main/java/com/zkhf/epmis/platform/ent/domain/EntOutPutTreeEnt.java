package com.zkhf.epmis.platform.ent.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 企业下排口，按排口类型分类-企业对象
 */
@Data
@Builder
public class EntOutPutTreeEnt {

    /**
     * 关联企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 企业简称
     */
    private String shorterName;

    /**
     * 企业排口类型列表
     */
    private List<EntOutPutTreeOutType> typeList;
}

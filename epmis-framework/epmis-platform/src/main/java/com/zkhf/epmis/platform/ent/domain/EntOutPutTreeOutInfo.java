package com.zkhf.epmis.platform.ent.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 企业下排口，按排口类型分类-排口对象
 */
@Data
@Builder
public class EntOutPutTreeOutInfo {

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 排放口编码
     */
    private String outPutCode;

    /**
     * 排放口名称
     */
    private String outPutName;
}

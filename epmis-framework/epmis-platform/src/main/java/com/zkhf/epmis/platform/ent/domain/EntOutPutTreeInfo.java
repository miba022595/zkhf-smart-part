package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

/**
 * 企业下排口，按排口类型分类-全量数据对象
 */
@Data
public class EntOutPutTreeInfo {

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

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
}

package com.zkhf.epmis.process.base.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

@Data
public class UserAttentionInfo {

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 排口编码
     */
    private String outPutCode;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
}

package com.zkhf.epmis.core.domain;

import lombok.Data;

import java.util.List;

@Data
public class AnnexReq {
    /**
     * 附件主键id
     */
    private String annexId;

    /**
     * 附件归属id
     */
    private List<String> sourceIds;
    /**
     * 附件归属id的类型
     */
    private String sourceType;
}

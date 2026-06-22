package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业清洁生产基础对象 t_ent_clean_produce
 */
@Data
public class EntCleanProduceReq {

    /**
     * 企业名称，模糊
     */
    private String entName;

    /**
     * 名称
     */
    private String cleanName;

    /**
     * 权限管理
     */
    private String entCode;
    private List<String> entCodes;
}

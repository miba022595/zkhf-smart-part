package com.zkhf.epmis.platform.base.domain;

import lombok.Data;

@Data
public class MonFactorInfo {
    /**
     * 监测因子
     */
    private String name;
    /**
     * 中文描述
     */
    private String desc;
    /**
     * 对应的数据字段
     */
    private String field;
}

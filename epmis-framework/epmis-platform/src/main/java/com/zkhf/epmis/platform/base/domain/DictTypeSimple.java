package com.zkhf.epmis.platform.base.domain;

import lombok.Data;

/**
 * 字典类型表 sys_dict_type
 */
@Data
public class DictTypeSimple {

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;
}

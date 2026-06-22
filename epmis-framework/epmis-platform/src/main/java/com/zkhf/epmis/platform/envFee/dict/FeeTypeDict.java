package com.zkhf.epmis.platform.envFee.dict;

import lombok.Data;

/**
 * 费用类型字典表实体类 t_bas_fee_type_dict
 */
@Data
public class FeeTypeDict {

    /**
     * 类型ID
     */
    private Integer id;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;
}

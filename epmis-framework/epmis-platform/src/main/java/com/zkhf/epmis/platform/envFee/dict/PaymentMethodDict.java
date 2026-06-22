package com.zkhf.epmis.platform.envFee.dict;

import lombok.Data;

/**
 * 支付方式字典表实体类 t_bas_payment_method_dict
 */
@Data
public class PaymentMethodDict {

    /**
     * 支付方式ID
     */
    private Integer id;

    /**
     * 支付方式代码
     */
    private String methodCode;

    /**
     * 支付方式名称
     */
    private String methodName;

    /**
     * 方式描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;
}
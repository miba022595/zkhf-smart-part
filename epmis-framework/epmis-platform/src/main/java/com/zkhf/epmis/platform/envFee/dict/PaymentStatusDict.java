package com.zkhf.epmis.platform.envFee.dict;

import lombok.Data;

/**
 * 付款状态字典表实体类 t_bas_payment_status_dict
 */
@Data
public class PaymentStatusDict {

    /**
     * 状态ID
     */
    private Integer id;

    /**
     * 状态代码
     */
    private String statusCode;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 状态描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;
}

package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 环保费用登记请求对象
 */
@Data
public class EnvFeeReq {

    /**
     * 归属企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /** 关联项目（环评环保管理-项目主键id） */
    private String projectId;

    /** 费用类型（源自 t_bas_fee_type_dict） */
    private String feeType;

    /** 缴费截至日期-开始时间 */
    private LocalDate paymentStart;
    /** 缴费截至日期-结束时间 */
    private LocalDate paymentEnd;

    /** 费用状态（源自 t_bas_fee_status_dict） */
    private String status;
}

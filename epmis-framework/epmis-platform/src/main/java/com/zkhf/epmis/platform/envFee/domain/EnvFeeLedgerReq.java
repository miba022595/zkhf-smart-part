package com.zkhf.epmis.platform.envFee.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 环保费用台账请求对象
 */
@Data
public class EnvFeeLedgerReq {

    /**
     * 归属企业编码
     */
    private String entCode;
    private List<String> entCodes;

    /** 费用类型（源自 t_bas_fee_type_dict） */
    private String feeType;

    /** 开始时间, yyyy-MM-dd */
    private String start;
    /** 结束时间, yyyy-MM-dd */
    private String end;

    /**
     * 筛选的状态列表
     */
    private List<String> statusList;
    /**
     * 月份列表
     */
    private Map<Integer, String> months;
}

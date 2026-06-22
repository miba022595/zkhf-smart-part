package com.zkhf.epmis.platform.envFee.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 环保费用登记对象 t_env_fees
 */
@Data
public class EnvFee {

    /** 费用记录ID */
    private String feeId;

    /**
     * 归属企业编码
     */
    private String entCode;
    private String entName;

    /** 关联项目（环评环保管理-项目主键id） */
    private String projectId;
    private String projectCode;
    private String projectName;

    /** 费用编号 */
    private String feeCode;

    /** 费用类型（源自 t_bas_fee_type_dict） */
    private String feeType;
    private String feeTypeName;
    private String feeTypeDesc;

    /** 费用金额 */
    private BigDecimal feeAmount;
    /** 开票金额（统计的开票金额减去红冲金额） */
    private BigDecimal invoiceAmount;
    /** 付款金额（统计支付完成金额和部分退款中的付款金额） */
    private BigDecimal paymentAmount;

    /** 缴费截至日期 */
    private LocalDate paymentDate;

    /** 费用状态（源自 t_bas_fee_status_dict） */
    private String status;
    private String statusName;
    private String statusDesc;

    /** 费用描述 */
    private String feeDesc;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 附件列表（查询时详情时用）
     */
    private List<AnnexInfo> annexInfoList;

}

package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资业务通用查询对象
 */
@Data
public class MaterialBizReq {

    /** 业务主键ID */
    private String id;

    /** 企业编码 */
    private String entCode;

    /** 企业编码列表 */
    private List<String> entCodes;

    /** 仓库ID */
    private String warehouseId;

    /** 单据编号 */
    private String orderNo;

    /** 物资名称 */
    private String materialName;

    /** 单据状态 */
    private Integer status;

    /** 审核状态 */
    private Integer auditStatus;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}

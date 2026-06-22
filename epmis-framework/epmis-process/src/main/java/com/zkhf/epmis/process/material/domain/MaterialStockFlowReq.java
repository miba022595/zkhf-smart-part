package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存流水查询对象
 */
@Data
public class MaterialStockFlowReq {

    /** 企业编码 */
    private String entCode;

    /** 企业编码列表 */
    private List<String> entCodes;

    /** 仓库ID */
    private String warehouseId;

    /** 物资ID */
    private String materialId;

    /** 业务类型：IN/OUT/RETURN/ADJUST/FREEZE/UNFREEZE */
    private String bizType;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}

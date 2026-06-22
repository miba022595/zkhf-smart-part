package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库信息对象
 */
@Data
public class MaterialWarehouse {

    /** 仓库ID */
    private String warehouseId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 仓库编号 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 仓库管理员 */
    private String managerName;

    /** 联系电话 */
    private String managerPhone;

    /** 仓库地址 */
    private String warehouseAddress;

    /** 排序号 */
    private Integer sortNum;

    /** 状态（0正常 1停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

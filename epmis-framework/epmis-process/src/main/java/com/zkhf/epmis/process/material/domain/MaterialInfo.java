package com.zkhf.epmis.process.material.domain;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资基础信息对象
 */
@Data
public class MaterialInfo {

    /** 物资ID */
    private String materialId;

    /** 企业编码 */
    private String entCode;

    /** 企业名称 */
    private String entName;

    /** 物资编号 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 品牌 */
    private String brand;

    /** 规格型号 */
    private String modelSpec;

    /** 物资分类编码 */
    private String categoryCode;

    /** 物资分类名称 */
    private String categoryName;

    /** 计量单位 */
    private String unit;

    /** 单价 */
    private Double unitPrice;

    /** 最低库存预警值 */
    private Double minStock;

    /** 当前库存（汇总） */
    private Double currentQty;

    /** 各仓库库存明细列表（非表字段） */
    private List<JSONObject> stockList;

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

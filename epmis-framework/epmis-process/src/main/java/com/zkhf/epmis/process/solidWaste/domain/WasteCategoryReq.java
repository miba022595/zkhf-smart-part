package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

import java.util.List;

/**
 * 固废种类管理对象 t_waste_category
 */
@Data
public class WasteCategoryReq {

    /** 主键id */
    private String categoryId;

    /** 所属企业 */
    private String entCode;
    private List<String> entCodes;

    /** 关联排口 */
    private String outPutId;

    /** 废物名称(具体名称模糊) */
    private String wasteName;

    /** 固废分类id树，向后模糊 */
    private String wasteDictId;

    /** 处置/处理方法 */
    private String disposalMethod;
}

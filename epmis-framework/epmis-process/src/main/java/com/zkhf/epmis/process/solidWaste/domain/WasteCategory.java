package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

/**
 * 固废种类管理对象 t_waste_category
 */
@Data
public class WasteCategory {

    /** 主键id */
    private String categoryId;

    /** 所属企业 */
    private String entCode;
    private String entName;

    /** 关联排口 */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 固废分类id树 */
    private String wasteDictId;
    /** 固废分类 */
    private String wasteCategory;
    /** 固废类别 */
    private String wasteType;
    /** 固废代码 */
    private String wasteCode;

    /** 废物名称(具体名称)，可默认填充选了的分类名称 */
    private String wasteName;

    /** 处置/处理方法 */
    private String disposalMethod;
    private String disposalDesc;

    /** 废物形态 */
    private String wasteForm;
    private String wasteFormDesc;

    /** 设计生产量(t/a) */
    private Double designOutput;

    /** 容器/包装类型 */
    private String packageType;
    private String packageTypeDesc;

    /** 主要成分 */
    private String mainComponent;

    /** 有害成分(危废专用) */
    private String hazardousComponent;

    /** 危险特性，多选 */
    private String hazardCharacteristic;
    private String hazardCharacteristicDesc;

    /** 注意事项（从字典上取到值，逗号拼接成字符串，可供修改） */
    private String precautions;
    private String precautionDesc;

    /** 应急措施 */
    private String emergencyMeasures;

    /** 委托处置单位(第三方单位) */
    private String disposalUnit;
    private String disposalUnitName;
}

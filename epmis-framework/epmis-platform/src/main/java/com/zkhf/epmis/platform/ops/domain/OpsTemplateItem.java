package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

import java.util.List;

/**
 * 运维模板配置-运维项对象 t_ops_template_item
 */
@Data
public class OpsTemplateItem {

    /** 运维项ID（主键） */
    private String templateItemId;

    /** 标题项名称 */
    private String itemName;

    /** 是否需要输入标题（1：需要，0：不需要） */
    private Integer needInputTitle;

    /** 是否显示标题（1：显示，0：不显示） */
    private Integer showTitle;

    /** 是否允许图片上传（1：允许，0：不允许） */
    private Integer allowImageUpload;

    /** 是否允许备注（1：允许，0：不允许） */
    private Integer allowRemark;

    /** 运维详情列表 */
    private List<OpsTemplateDetail> detailList;

}

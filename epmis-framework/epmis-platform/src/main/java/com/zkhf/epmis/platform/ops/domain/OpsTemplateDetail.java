package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

/**
 * 运维模板配置-运维内容对象 t_ops_template_detail
 */
@Data
public class OpsTemplateDetail {

    /** 运维内容ID（主键） */
    private String templateDetailId;

    /** 子项名称 */
    private String detailName;

    /** 输入方式（1：文本，2：数字，3：选择，4：开关） */
    private Integer inputType;

    /** 选项列表（JSON格式，用于选择型输入） */
    private String options;

}

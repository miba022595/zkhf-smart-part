package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运维模板配置 模板表 t_ops_template
 */
@Data
public class OpsTemplate {

    /** 关联企业 */
    private String entCode;
    private String entName;

    /** 关联排口 */
    private String outPutId;
    private String outPutCode;
    private String outPutName;

    /** 运维类型编码 */
    private String templateCode;

    /** 运维类型名称（如：设备巡检、排放口维护等） */
    private String templateName;

    /** 显示附件标记（1：需要，0：不需要） */
    private Integer showAttachmentFlag;

    /** 显示是否合格标志（1：需要，0：不需要） */
    private Integer showQualifiedFlag;

    /** 提示语，为空时不显示 */
    private String promptText;

    /** 运维项列表 */
    @JsonIgnore
    private String itemArray;
    private List<OpsTemplateItem> itemList;

    /** 模板的备注说明 */
    private String remark;

    /** 是否可修改 */
    private boolean modify;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}

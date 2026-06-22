package com.zkhf.epmis.platform.ent.domain;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 第三方单位对象 t_ext_unit_lib
 */
@Data
public class ExtUnit {

    /** 统一社会信用代码 */
    private String unitCode;

    /** 单位名称 */
    private String unitName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactNumber;

    /** 缴纳社保人数 */
    private Integer socialNum;

    /** 注册地址 */
    private String registerAddr;

    /** 资质开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certStart;

    /** 资质结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certEnd;

    /** 服务内容 */
    private String servItem;

    /** 其他的信息 */
    @JsonIgnore
    private String extraInfoStr;
    private JSONObject extraInfo;

    /** 附件列表（更新时用） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;
}

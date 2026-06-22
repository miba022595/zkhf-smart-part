package com.zkhf.epmis.platform.base.domain;

import lombok.Data;

import java.util.List;

/**
 * 国民经济行业分类对象 t_bas_industry_category
 */
@Data
public class IndustryCategory {

    /**
     * 主键id
     */
    private String id;

    /**
     * 父级id
     */
    private String pid;

    /**
     * 代码
     */
    private String code;

    /**
     * 类别名称
     */
    private String name;

    /**
     * 说明
     */
    private String desc;

    /**
     * 下级列表
     */
    private List<IndustryCategory> sub;
}

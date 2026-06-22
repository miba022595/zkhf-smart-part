package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

/**
 * 固废分类字典对象 t_waste_dict
 */
@Data
public class WasteDict {

    /** 编号 */
    private Long id;

    /** 上级编号 */
    private Long pid;

    /** 名称 */
    private String name;

    /** 代码(HW08/SW17等) */
    private String code;

    /** 固废代码(900-214-08等) */
    private String tag;

    /** 扩展名 */
    private String extName;
}
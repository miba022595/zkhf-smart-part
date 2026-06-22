package com.zkhf.epmis.platform.base.domain;

import lombok.Data;

/**
 * 地区对象 t_bas_districts
 */
@Data
public class Districts {

    /** 编号 */
    private Long id;

    /** 上级编号 */
    private Long pid;

    /** 名称 */
    private String name;

    /** 拼音 */
    private String pinyin;

    /** 扩展名 */
    private String extName;
}

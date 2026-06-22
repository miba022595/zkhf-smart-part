package com.zkhf.epmis.platform.ent.domain;

import lombok.Data;

/**
 * 第三方单位查询对象
 */
@Data
public class ExtUnitReq {

    /** 登录账号id */
    private Long userId;

    /** 单位名称-模糊 */
    private String unitName;
}

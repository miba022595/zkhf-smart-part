package com.zkhf.epmis.process.valid.domain;

import com.zkhf.epmis.process.base.domain.EntInfo;
import lombok.Data;

import java.util.List;

/**
 * 企业资质有效期预警数据对象 t_valid_period_info
 */

@Data
public class ValidPeriodReq {

    /** 企业唯一编码 */
    private String entCode;
    private List<String> entCodes;

    /** 资质证件类型 */
    private Integer confType;

    /** 是否用临时表 */
    private boolean temporary;

    /** 临时表名 */
    private String tableName;

    /** 权限列表 */
    List<EntInfo> entList;
}

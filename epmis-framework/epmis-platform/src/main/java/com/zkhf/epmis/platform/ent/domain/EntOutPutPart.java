package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

/**
 * 企业排口对象 t_bas_ent_out_put_info
 */
@Data
public class EntOutPutPart {

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 排放口编码
     */
    private String outPutCode;

    /**
     * 排放口名称
     */
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 打卡签到范围，默认100米
     */
    private Integer clockRange;
}

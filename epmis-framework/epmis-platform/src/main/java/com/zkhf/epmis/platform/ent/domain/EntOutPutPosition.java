package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

/**
 * 企业排口对象（包含位置信息）
 */
@Data
public class EntOutPutPosition {

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 关联企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

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
     * 排放口设备mn号
     */
    private String mnNum;

    /**
     * 排放口状态，字典out_put_status
     * 参见 {@link OutPutStatusEnum}
     */
    private Integer outPutStatus;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 排放口高度
     */
    private Double outPutHeight;

    /**
     * 排放口位置
     */
    private String outPutPosition;

    /**
     * 是否安装在线设备，1 是，0 否
     */
    private Integer installOnline;
}

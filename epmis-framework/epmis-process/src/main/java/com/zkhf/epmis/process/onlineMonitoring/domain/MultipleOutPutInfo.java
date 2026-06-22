package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * 排口公共的信息
 */
@Data
@Builder
public class MultipleOutPutInfo {

    /** 排口主键id */
    private String outPutId;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /** 企业名称 */
    private String entCode;
    /** 企业名称 */
    private String entName;

    /** 排口编码 */
    private String outPutCode;
    /** 排口名称 */
    private String outPutName;

    /** 检测设备mn号 */
    private String mnNum;

    /** 查询的表名 */
    @JsonIgnore
    private String tableName;
}

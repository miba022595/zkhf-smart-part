package com.zkhf.epmis.process.base.domain;

import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import lombok.Data;

/**
 * 企业排口报警参数
 */
@Data
public class OutPutAlarmConf {

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 报警级别-报警类型
     * {@link AlarmDetailTypeEnum}
     */
    private Integer alarmCode;

    /**
     * 排放口状态，字典out_put_status
     * 参见 {@link OutPutStatusEnum}
     */
    private String outPutStatus;

    /**
     * 启动标志，0不启动、1启动
     */
    private Integer isEnabled;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 持续时长(分钟)
     */
    private Integer dataDur;

    /**
     * 持续条数
     */
    private Integer dataCycle;
}

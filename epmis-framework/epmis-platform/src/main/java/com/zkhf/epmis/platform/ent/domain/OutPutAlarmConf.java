package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.AlarmDetailTypeEnum;
import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import lombok.Data;

/**
 * 企业排口报警参数 t_ent_out_put_alarm
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
    private String alarmDesc;

    /**
     * 排放口状态，字典out_put_status
     * 参见 {@link OutPutStatusEnum}
     */
    private String outPutStatus;
    private String outPutStatusDesc;

    /**
     * 启动标志，0不启动、1启动
     */
    private Integer isEnabled;

    /**
     * 数据类型
     */
    private String dataType;
    private String dataTypeDesc;

    /**
     * 持续时长(分钟)
     */
    private Integer dataDur;

    /**
     * 持续条数
     */
    private Integer dataCycle;
}

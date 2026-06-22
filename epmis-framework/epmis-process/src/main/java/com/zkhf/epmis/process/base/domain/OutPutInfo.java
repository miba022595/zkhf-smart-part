package com.zkhf.epmis.process.base.domain;

import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

@Data
public class OutPutInfo {

    /**
     * 排口主键id
     */
    private String outPutId;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 所在地区
     */
    private String region;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 排口编码
     */
    private String outPutCode;

    /**
     * 排口名称
     */
    private String outPutName;

    /**
     * 检测设备mn号
     */
    private String mnNum;

    /**
     * 关联企业微信
     */
    private String weComMsg;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 排口状态
     * {@link OutPutStatusEnum}
     */
    private Integer outPutStatus;

    /**
     * 数据传输率
     */
    private Float transRate;

    /**
     * 数据有效率
     */
    private Float validRate;

    /**
     * 分钟数据间隔，n分钟，一般配置为5、10分钟
     * 60/值，取整数部分当作小时的分钟数据条数
     */
    private Integer minuteDataInterval;

    /**
     * 报警对应人id
     */
    private Long perAlarmId;

    /**
     * 报警对应人姓名
     */
    private String perAlarmName;

    /**
     * 报警对应人联系方式-电话
     */
    private String perAlarmPhone;

    /**
     * 报警对应人联系方式-邮箱
     */
    private String perAlarmEmail;
}

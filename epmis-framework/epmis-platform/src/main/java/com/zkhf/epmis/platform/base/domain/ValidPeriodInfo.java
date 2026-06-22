package com.zkhf.epmis.platform.base.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ValidPeriodInfo {

    /**
     * 企业唯一编码
     */
    private String entCode;

    // ************** 预警配置用的字段 ******************

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 企业微信关联信息
     */
    private String weComMsg;

    /**
     * 资质证件类型:1-营业执照 2-经营许可证 3-卫生许可证 4-安全生产许可证...
     */
    private Integer confType;

    /**
     * 证书主键，唯一标识用
     */
    private String itemId;

    /**
     * 证书名称
     */
    private String itemName;

    /**
     * 黄色提醒阈值(剩余天数≤30天触发)
     */
    private Integer yellowThreshold;

    /**
     * 黄色提醒频率(每5天1次)
     * 格式:D-天/M-月/H-小时+数字,如D10=每10天1次
     */
    private String yellowNotifyFreq;

    /**
     * 橙色预警阈值(剩余天数≤10天触发)
     */
    private Integer orangeThreshold;

    /**
     * 橙色预警频率(每天1次)
     */
    private String orangeNotifyFreq;

    /**
     * 红色报警阈值(剩余天数≤3天触发)
     */
    private Integer redThreshold;

    /**
     * 红色报警频率(每3小时1次)
     */
    private String redNotifyFreq;

    // ************** 资质期限用的字段 ******************

    /**
     * 资质有效期-开始时间
     */
    private LocalDate beginDate;

    /**
     * 资质有效期-结束时间
     */
    private LocalDate endDate;

    // ************** 手工监测任务用的字段 ******************

    /**
     * 排口名称
     */
    private String outPutName;

    /**
     * 污染因子名称--中文
     */
    private String pollutantNameCn;

    /**
     * 计划检测频次，参考CheckFrequencyType
     */
    private Integer checkFrequency;
}

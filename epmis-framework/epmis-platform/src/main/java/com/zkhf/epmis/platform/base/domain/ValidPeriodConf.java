package com.zkhf.epmis.platform.base.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)对象 t_valid_period_conf
 */
@Data
public class ValidPeriodConf {

    /**
     * 企业唯一编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 资质证件类型:1-营业执照 2-经营许可证 3-卫生许可证 4-安全生产许可证...
     */
    private Integer confType;

    /**
     * 资质证件类型描述
     */
    private String confDesc;

    /**
     * 黄色提醒阈值(剩余天数≤30天触发)
     */
    private Integer yellowThreshold;

    /**
     * 黄色提醒频率(每5天1次)
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

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

package com.zkhf.epmis.process.alarm.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报警处理情况表 t_data_out_alarm_deal
 */
@Data
public class DurAlarmDeal {

    /** 主键id */
    private String dealId;

    /** 处理账号 */
    private Long dealUserId;
    private String dealUserName;

    /** 处理信息 */
    private String dealInfo;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dealTime;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 附件列表（查询时详情时用）
     */
    private List<AnnexInfo> annexList;
}
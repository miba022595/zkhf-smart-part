package com.zkhf.epmis.platform.envProtect.policy.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 环境政策法规信息学习管理 t_env_learn
 */
@Data
public class EnvLearn {

    /**
     * 主键id
     */
    private String learnId;

    /**
     * 学习主题
     */
    private String learnTheme;

    /**
     * 学习政策法规
     */
    private List<EnvLearnPolicy> policyList;

    /**
     * 学习企业
     */
    private List<EnvLearnEnt> entList;

    /**
     * 学习人员
     */
    private List<EnvLearnUser> userList;

    /**
     * 学习要求时长，分钟
     */
    private Long requiredDuration;

    /**
     * 学习开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate learnStart;

    /**
     * 学习结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate learnEnd;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 学习人员个数
     */
    private Integer userNum;

    /**
     * 学习完成时长，分钟（平均值）
     */
    private Long completedDuration;

    /**
     * 完成进度，完成时长/要求时长
     */
    private Double learnRate;
}

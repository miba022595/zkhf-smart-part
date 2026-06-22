package com.zkhf.epmis.platform.envProtect.policy.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习详情 t_env_learn_detail
 */
@Data
public class LearnDetail {

    /**
     * 学习详情主键id
     */
    private String learnDetailId;

    /**
     * 学习开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime learnStart;

    /**
     * 学习结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime learnEnd;

    /**
     * 本次学习时间，分钟
     */
    private Integer duration;

    /**
     * 本次学习的文件主键id
     */
    private String annexId;

    /**
     * 本次学习的文件的名称，包括扩展名
     */
    private String fileName;
}

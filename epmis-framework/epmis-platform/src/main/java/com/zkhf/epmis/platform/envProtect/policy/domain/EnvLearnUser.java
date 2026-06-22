package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

/**
 * 环境政策法规信息学习情况统计对象 t_env_learn_user
 */
@Data
public class EnvLearnUser {

    /**
     * 主键id
     */
    private String learnUserId;

    /**
     * 学习id
     */
    private String learnId;

    /**
     * 学习人员id（账户id）
     */
    private Long userId;
    private String userName;

    /**
     * 学习完成时长，分钟
     */
    private Long completedDuration;

    /**
     * 学习要求时长，分钟
     */
    private Long requiredDuration;

    /**
     * 学习进度
     */
    private Double learnRate;

}
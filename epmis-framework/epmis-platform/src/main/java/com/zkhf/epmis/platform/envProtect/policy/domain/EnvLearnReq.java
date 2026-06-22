package com.zkhf.epmis.platform.envProtect.policy.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习管理请求参数
 */
@Data
public class EnvLearnReq {

    /**
     * 学习法规标准名称
     */
    private String policyName;

    /**
     * 学习主题-模糊
     */
    private String learnTheme;

    /**
     * 账户权限：有userId，非管理员账户，entAdmin为true：
     */
    private Long userId;
    private boolean entAdmin;

    /* ********** 学习进度更新 ************* */

    /**
     * 学习详情主键id，为空时表示新的进度数据
     */
    private String learnDetailId;

    /**
     * 是不是新的学习详情，1新的
     */
    private int newLearnDetail;

    /**
     * 学习统计主键id
     */
    private String learnUserId;

    /**
     * 学习的附件id
     */
    private String annexId;

    /** 学习时间 */
    private LocalDateTime learnDate = LocalDateTime.now();
}

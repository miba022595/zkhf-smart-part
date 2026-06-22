package com.zkhf.epmis.process.homePage.domain;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 首页标识情况统计
 */
@Data
public class HomePageSignInfo {

    /**
     * 达标率
     */
    private HomePageSignInfoDetail achievement;

    /**
     * 传输有效率
     */
    private HomePageSignInfoDetail effectiveTrans;

    /**
     * 标记完成率
     */
    private HomePageSignInfoDetail completion;

    /**
     * 超标次数
     */
    private Integer overLimit;
}

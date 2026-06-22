package com.zkhf.epmis.process.homePage.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 首页标识情况统计
 */
@Data
public class HomePageSignInfoDetail {
    // 总数
    private Integer total;
    // 已完成
    private Integer complete;
    // 比率
    private Float rate;
}

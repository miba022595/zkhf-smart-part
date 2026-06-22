package com.zkhf.epmis.core.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeadInfo {
    /**
     * 监测因子
     */
    private String name;
    /**
     * 中文描述
     */
    private String desc;
}

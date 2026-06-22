package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

@Data
public class OpsRecordDetail {

    /** 运维内容ID（主键） */
    private String templateDetailId;

    /** 记录信息 */
    private Object inputValue;
}

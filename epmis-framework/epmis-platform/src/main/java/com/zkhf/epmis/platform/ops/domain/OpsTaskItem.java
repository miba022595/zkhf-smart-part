package com.zkhf.epmis.platform.ops.domain;

import lombok.Data;

import java.util.List;

/**
 * 运维任务配置-运维项对象
 */
@Data
public class OpsTaskItem {

    /** 运维项ID（主键） */
    private String templateItemId;

    /** 运维详情列表 */
    private List<OpsTaskDetail> detailList;

}

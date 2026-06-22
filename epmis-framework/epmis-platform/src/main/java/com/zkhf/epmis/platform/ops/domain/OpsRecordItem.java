package com.zkhf.epmis.platform.ops.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import lombok.Data;

import java.util.List;

@Data
public class OpsRecordItem {

    /** 运维项记录ID（主键） */
    private String recordItemId;

    /** 运维项ID（主键） */
    private String templateItemId;

    /** 输入的标题名称 */
    private String titleValue;

    /** 备注信息 */
    private String remark;

    /** 运维记录详情列表 */
    private List<OpsRecordDetail> detailList;

    /**
     * 附件列表（更新时用），附件类型 opsRecordItem
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 附件列表（查询时用）
     */
    private List<AnnexInfo> annexInfoList;
}

package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 企业下排口，按排口类型分类-排口类型对象
 */
@Data
@Builder
public class EntOutPutTreeOutType {

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;
    private String outPutTypeDesc;

    /**
     * 排口列表
     */
    private List<EntOutPutTreeOutInfo> outList;
}

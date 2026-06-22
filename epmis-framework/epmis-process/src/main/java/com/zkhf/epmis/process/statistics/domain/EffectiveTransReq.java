package com.zkhf.epmis.process.statistics.domain;

import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class EffectiveTransReq {

    /** 所属企业-权限 */
    private String entCode;
    private List<String> entCodes;

    /** 排口主键id */
    private List<String> outPutIdList;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 数据类型
     * 参见 {@link DataTypeEnum}
     */
    private Integer dataType;

    /**
     * 污染物监测时间，开始日期, yyyy-MM-dd HH
     */
    private String beginTime;

    /**
     * 污染物监测时间，结束日期, yyyy-MM-dd HH
     */
    private String endTime;
}

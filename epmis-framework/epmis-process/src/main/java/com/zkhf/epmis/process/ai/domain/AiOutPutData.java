package com.zkhf.epmis.process.ai.domain;

import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import lombok.Data;

import java.util.List;

/**
 * 多排口查询后的数据
 */
@Data
public class AiOutPutData {

    /** 排口信息 */
    private OutPutInfo outInfo;

    /** 数据表头信息 */
    private List<PollHead> headList;

    /** 数据信息 */
    private List<DataInfo> dataList;
}

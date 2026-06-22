package com.zkhf.epmis.platform.mapper.base;

import com.zkhf.epmis.platform.base.domain.PollutantCode;
import com.zkhf.epmis.platform.base.domain.PollutantCodeReq;

import java.util.List;

/**
 * 数采报文对应的污染因子关系 2017版本和2003版Mapper接口
 */
public interface PollutantCodeMapper {

    /**
     * 查询数采报文对应的污染因子关系 2017版本和2003版列表
     */
    List<PollutantCode> selectPollutantCodeList(PollutantCodeReq req);
}

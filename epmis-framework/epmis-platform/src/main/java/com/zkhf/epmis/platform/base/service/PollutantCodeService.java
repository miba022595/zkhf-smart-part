package com.zkhf.epmis.platform.base.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.PollutantCode;
import com.zkhf.epmis.platform.base.domain.PollutantCodeReq;

import java.util.List;
import java.util.Map;

/**
 * 数采报文对应的污染因子关系 2017版本和2003版Service接口
 */
public interface PollutantCodeService {

    /**
     * 查询数采报文对应的污染因子关系 2017版本和2003版列表
     */
    AjaxResult selectPollutantCodeList(PollutantCodeReq req);

    /**
     * 查询污染因子code&名称的关系
     */
    Map<String, String> selectPollutantCodeName();

    /**
     * 查询污染因子code&名称的关系
     */
    Map<String, PollutantCode> selectAllPollCodeMap();

    /**
     * 查询所有污染因子信息
     */
    List<PollutantCode> selectAllPollCodeList();
}

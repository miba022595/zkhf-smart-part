package com.zkhf.epmis.platform.base.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.platform.base.domain.PollutantCode;
import com.zkhf.epmis.platform.base.domain.PollutantCodeReq;
import com.zkhf.epmis.platform.base.service.PollutantCodeService;
import com.zkhf.epmis.platform.mapper.base.PollutantCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数采报文对应的污染因子关系 2017版本和2003版Service业务层处理
 */
@Service
public class PollutantCodeServiceImpl implements PollutantCodeService {

    private PollutantCodeMapper pollutantCodeMapper;
    @Autowired
    public void setPollutantCodeMapper(PollutantCodeMapper pollutantCodeMapper) {
        this.pollutantCodeMapper = pollutantCodeMapper;
    }

    @Override
    public AjaxResult selectPollutantCodeList(PollutantCodeReq req) {
        boolean page = PageUtils.startPageCheckExists();
        List<PollutantCode> list = pollutantCodeMapper.selectPollutantCodeList(req);
        list.forEach( e -> {
            if (null != e.getMonFactor()) {
                String monFactor = e.getMonFactor().toString();
                e.setMonFactor(JSONArray.parseArray(monFactor));
            }
        });
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public Map<String, String> selectPollutantCodeName() {
        // 获取所有污染物code和name的对应关系
        List<PollutantCode> codeList = pollutantCodeMapper.selectPollutantCodeList(null);
        Map<String, String> codeMap = new HashMap<>();
        if (null != codeList && !codeList.isEmpty()) {
            codeList.forEach( e -> codeMap.put(e.getPollutantCode(), e.getPollutantNameCn()));
        }
        return codeMap;
    }

    @Override
    public Map<String, PollutantCode> selectAllPollCodeMap() {
        // 获取所有污染物code和name的对应关系
        List<PollutantCode> codeList = pollutantCodeMapper.selectPollutantCodeList(null);
        Map<String, PollutantCode> codeMap = new HashMap<>();
        if (null != codeList && !codeList.isEmpty()) {
            codeList.forEach( e -> codeMap.put(e.getPollutantCode(), e));
        }
        return codeMap;
    }

    @Override
    public List<PollutantCode> selectAllPollCodeList() {
        return pollutantCodeMapper.selectPollutantCodeList(null);
    }
}

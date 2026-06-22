package com.zkhf.epmis.platform.homePage.service.impl;

import cn.hutool.core.map.MapUtil;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.homePage.service.HomePageService;
import com.zkhf.epmis.platform.mapper.homePage.HomePageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页 Service业务层处理
 */
@Slf4j
@Service
public class HomePageServiceImpl implements HomePageService {

    private HomePageMapper homePageMapper;
    @Autowired
    public void setHomePageMapper(HomePageMapper homePageMapper) {
        this.homePageMapper = homePageMapper;
    }

    @Override
    public AjaxResult cockpitEnvGovernanceStatistics(String entCode) {
        AjaxResult result = AjaxResult.success();
        List<Map<String, Object>> dataList = new ArrayList<>();
        result.put("data", dataList);
        if (StringUtils.isEmpty(entCode)) {
            return result;
        }
        List<Map<String, Object>> list = homePageMapper.cockpitEnvGovernanceStatistics(entCode);
        list.forEach( e -> {
            String outPutTypeDesc = OutPutTypeEnum.getNameByCode(MapUtil.getInt(e, "outPutType"));
            if (StringUtils.isNotEmpty(outPutTypeDesc)) {
                Map<String, Object> data = new HashMap<>();
                data.put("outPutType", outPutTypeDesc);
                data.put("governanceCount", MapUtil.getInt(e, "governanceCount"));
                dataList.add(data);
            }
        });
        return result;
    }

    @Override
    public AjaxResult cockpitEnvMonitorStatistics(String entCode) {
        AjaxResult result = AjaxResult.success();
        List<Map<String, Object>> dataList = new ArrayList<>();
        result.put("data", dataList);
        if (StringUtils.isEmpty(entCode)) {
            return result;
        }
        List<Map<String, Object>> list = homePageMapper.cockpitEnvMonitorStatistics(entCode);
        list.forEach( e -> {
            String outPutTypeDesc = OutPutTypeEnum.getNameByCode(MapUtil.getInt(e, "outPutType"));
            if (StringUtils.isNotEmpty(outPutTypeDesc)) {
                Map<String, Object> data = new HashMap<>();
                data.put("outPutType", outPutTypeDesc);
                data.put("monitorCount", MapUtil.getInt(e, "monitorCount"));
                dataList.add(data);
            }
        });
        return result;
    }
}

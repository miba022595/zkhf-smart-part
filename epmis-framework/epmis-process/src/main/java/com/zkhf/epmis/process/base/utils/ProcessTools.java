package com.zkhf.epmis.process.base.utils;

import cn.hutool.core.map.MapUtil;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.OutPutPollInfo;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共数据处理工具类
 */
@Slf4j
@Component
public class ProcessTools {

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    /**
     * 查询企业资质的排放量信息
     */
    public Map<String, Map<String, BigDecimal>> getEntYLimitMap(Integer permitYear) {
        // 查询企业资质的排放量信息
        List<Map<String, Object>> countList = platformFacade.selectAllEntOutPollutantPermitCount(permitYear);
        // 企业年排量限值
        Map<String, Map<String, BigDecimal>> entYLimitMap = new HashMap<>();
        for (Map<String, Object> map : countList) {
            String entCode = MapUtil.getStr(map, "entCode");
            if (StringUtils.isEmpty(entCode)) {
                continue;
            }
            String pollutantCode = MapUtil.getStr(map, "pollutantCode");
            if (StringUtils.isEmpty(pollutantCode)) {
                continue;
            }
            String permitCountVal = MapUtil.getStr(map, "permitCount");
            if (StringUtils.isEmpty(permitCountVal)) {
                continue;
            }
            BigDecimal permitCount = new BigDecimal(permitCountVal);
            if (!entYLimitMap.containsKey(entCode)) {
                entYLimitMap.put(entCode, new HashMap<>());
            }
            Map<String, BigDecimal> entSub = entYLimitMap.computeIfAbsent(entCode, k -> new HashMap<>());
            if (entSub.containsKey(pollutantCode)) {
                entSub.put(pollutantCode, entSub.get(pollutantCode).max(permitCount));
            } else {
                entSub.put(pollutantCode, permitCount);
            }
        }
        return entYLimitMap;
    }

    public BigDecimal getToNowLimitValue(OutPutPollInfo info, int monthValue) {
        if (null == info.getMonthlyLimitValue()) {
            return null;
        }
        String[] value = info.getMonthlyLimitValue().split(",");
        if (value.length != 12 || monthValue > value.length) {
            return null;
        }
        BigDecimal toNowLimitValue = null;
        for (int i = 0; i < monthValue; i++) {
            String v = value[i];
            if (StringUtils.isEmpty(v)) {
                continue;
            }
            if (toNowLimitValue == null) {
                toNowLimitValue = new BigDecimal(value[i]);
            } else {
                toNowLimitValue = toNowLimitValue.add(new BigDecimal(value[i]));
            }
        }
        return toNowLimitValue;
    }

}

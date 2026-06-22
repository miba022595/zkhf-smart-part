package com.zkhf.epmis.platform.base.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import com.zkhf.epmis.platform.base.domain.ValidPeriodConf;

import java.util.List;

/**
 * 企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)Service接口
 */
public interface ValidPeriodConfService {

    /**
     * 查询企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)列表
     */
    AjaxResult selectValidPeriodConfList(String entCode);

    /**
     * 修改企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)
     */
    AjaxResult updateValidPeriodConf(ValidPeriodConf info);

    /**
     * 查询所有配置-feign
     */
    List<ValidPeriodAlarmInfo> feignConfList();
}

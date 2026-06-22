package com.zkhf.epmis.platform.base.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.Districts;

import java.util.List;

/**
 * 地区Service接口
 */
public interface DistrictsService {

    /**
     * 查询地区列表-单层
     */
    AjaxResult selectDistrictsSingleList(Long pid);

    /**
     * 查询地区列表
     */
    List<Districts> selectDistrictsList();

    /**
     * 新增地区
     */
    AjaxResult insertDistricts(Districts info);

    /**
     * 修改地区
     */
    AjaxResult updateDistricts(Districts info);

    /**
     * 删除地区信息
     */
    AjaxResult deleteDistrictsById(Long id);
}

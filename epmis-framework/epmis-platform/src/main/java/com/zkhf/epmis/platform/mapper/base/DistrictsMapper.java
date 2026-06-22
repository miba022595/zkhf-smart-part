package com.zkhf.epmis.platform.mapper.base;

import com.zkhf.epmis.platform.base.domain.Districts;

import java.util.List;

/**
 * 地区Mapper接口
 */
public interface DistrictsMapper {

    /**
     * 查询地区列表-单层
     */
    List<Districts> selectDistrictsSingleListByPid(Long pid);

    /**
     * 查询地区列表
     */
    List<Districts> selectDistrictsList();

    /**
     * 新增地区
     */
    void insertDistricts(Districts info);

    /**
     * 修改地区
     */
    void updateDistricts(Districts info);

    /**
     * 删除地区信息
     */
    void deleteDistrictsById(Long id);
}

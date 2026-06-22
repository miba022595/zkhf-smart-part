package com.zkhf.epmis.platform.mapper.ent;

import com.zkhf.epmis.platform.ent.domain.EntProduceFacility;
import com.zkhf.epmis.platform.ent.domain.EntProduceFacilityReq;

import java.util.List;

/**
 * 企业生产设施/设备Mapper接口
 */
public interface EntProduceFacilityMapper {

    /**
     * 查询企业生产设施/设备列表
     */
    List<EntProduceFacility> selectEntProduceFacilityList(EntProduceFacilityReq req);

    /**
     * 新增企业生产设施/设备
     */
    int insertEntProduceFacility(EntProduceFacility info);

    /**
     * 修改企业生产设施/设备
     */
    int updateEntProduceFacility(EntProduceFacility info);

    /**
     * 删除企业生产设施/设备
     */
    int deleteEntProduceFacilityById(String facilityId);

    /**
     * 删除企业生产设施/设备的关联信息
     */
    void deleteEntProduceFacilityRelateById(String facilityId);

}

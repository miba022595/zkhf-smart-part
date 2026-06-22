package com.zkhf.epmis.platform.mapper.ent;

import java.util.List;
import java.util.Map;

import com.zkhf.epmis.platform.ent.domain.EntPollControlFacility;
import com.zkhf.epmis.platform.ent.domain.EntPollControlFacilityReq;
import org.apache.ibatis.annotations.Param;

/**
 * 企业污染治理设施Mapper接口
 */
public interface EntPollControlFacilityMapper {

    /**
     * 查询企业污染治理设施列表
     */
    List<EntPollControlFacility> selectEntPollControlFacilityList(EntPollControlFacilityReq req);

    /**
     * 查询企业污染治理设施关联的排口信息
     */
    List<Map<String, Object>> selectRelateOutPutList(@Param("otherType") String otherType, @Param("facilityIds") List<String> facilityIds);

    /**
     * 新增企业污染治理设施
     */
    int insertEntPollControlFacility(EntPollControlFacility info);

    /**
     * 修改企业污染治理设施
     */
    int updateEntPollControlFacility(EntPollControlFacility info);

    /**
     * 删除企业污染治理设施
     */
    int deleteEntPollControlFacilityById(String facilityId);

    /**
     * 删除企业污染治理设施的关联
     */
    void deleteEntPollControlFacilityRelateById(String facilityId);

}

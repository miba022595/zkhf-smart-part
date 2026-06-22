package com.zkhf.epmis.platform.mapper.ent;

import java.util.List;
import java.util.Map;

import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshop;
import com.zkhf.epmis.platform.ent.domain.EntProduceWorkshopReq;
import org.apache.ibatis.annotations.Param;

/**
 * 企业生产车间Mapper接口
 */
public interface EntProduceWorkshopMapper {

    /**
     * 查询企业生产车间列表
     */
    List<EntProduceWorkshop> selectEntProduceWorkshopList(EntProduceWorkshopReq req);

    /**
     * 查询企业生产车间关联其他列表
     */
    List<Map<String, Object>> selectEntProduceWorkshopRelateList(@Param("otherType") String otherType, @Param("entCodes") List<String> entCodes, @Param("workshopIds") List<String> workshopIds);

    /**
     * 新增企业生产车间
     */
    int insertEntProduceWorkshop(EntProduceWorkshop info);

    /**
     * 修改企业生产车间
     */
    int updateEntProduceWorkshop(EntProduceWorkshop info);

    /**
     * 删除企业生产车间
     */
    int deleteEntProduceWorkshopById(String workshopId);

    /**
     * 删除旧的关联生产设施关系
     */
    void deleteRelateProduceFacilityById(@Param("otherType") String otherType, @Param("otherId") String otherId);

    /**
     * 删除旧的关联污染治理设施关系
     */
    void deleteRelatePollControlFacilityById(@Param("otherType") String otherType, @Param("otherId") String otherId);

    /**
     * 添加新的关联生产设施关系
     */
    void insertRelateProduceFacility(@Param("otherType") String otherType, @Param("otherId") String otherId, @Param("facilityIds") List<String> facilityIds);

    /**
     * 添加新的关联污染治理设施关系
     */
    void insertRelatePollControlFacility(@Param("otherType") String otherType, @Param("otherId") String otherId, @Param("facilityIds") List<String> facilityIds);

}

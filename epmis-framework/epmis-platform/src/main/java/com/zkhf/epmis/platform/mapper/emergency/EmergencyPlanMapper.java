package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyPlan;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlanReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应急预案Mapper。
 * 负责应急预案数据的查询、维护和批量导入写入。
 */
public interface EmergencyPlanMapper {

    /**
     * 新增应急预案。
     *
     * @param emergencyPlan 预案信息
     * @return 影响行数
     */
    int insert(EmergencyPlan emergencyPlan);

    /**
     * 批量新增应急预案。
     *
     * @param list 预案列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<EmergencyPlan> list);

    /**
     * 更新应急预案。
     *
     * @param emergencyPlan 预案信息
     * @return 影响行数
     */
    int update(EmergencyPlan emergencyPlan);

    /**
     * 根据预案ID删除记录。
     *
     * @param planId 预案ID
     * @return 影响行数
     */
    int deleteById(@Param("planId") String planId);

    /**
     * 根据预案ID查询详情。
     *
     * @param planId 预案ID
     * @return 预案详情
     */
    EmergencyPlan selectById(@Param("planId") String planId);

    /**
     * 按条件查询应急预案列表。
     *
     * @param req 查询条件
     * @return 预案列表
     */
    List<EmergencyPlan> selectList(EmergencyPlanReq req);

    /**
     * 查询全部应急预案。
     *
     * @return 预案列表
     */
    List<EmergencyPlan> selectAll();
}

package com.zkhf.epmis.process.mapper.solidWaste;

import com.zkhf.epmis.process.solidWaste.domain.WasteLibReq;
import com.zkhf.epmis.process.solidWaste.domain.WasteTotalPlan;

import java.util.List;

/**
 * 固废总量控制计划Mapper接口
 */
public interface WasteTotalPlanMapper {

    /**
     * 查询固废总量控制计划列表
     */
    List<WasteTotalPlan> selectWasteTotalPlanList(WasteLibReq req);

    /**
     * 新增固废总量控制计划
     */
    int insertWasteTotalPlan(WasteTotalPlan info);

    /**
     * 修改固废总量控制计划
     */
    int updateWasteTotalPlan(WasteTotalPlan info);

    /**
     * 删除固废总量控制计划
     */
    int deleteWasteTotalPlan(WasteTotalPlan info);
}
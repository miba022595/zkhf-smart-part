package com.zkhf.epmis.platform.mapper.envManual;

import java.util.List;

import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlan;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlanReq;

/**
 * 环境手工检测计划Mapper接口
 */
public interface EnvManualCheckPlanMapper {

    /**
     * 查询环境手工检测计划
     */
    EnvManualCheckPlan selectEnvManualCheckPlanByOutPutPollId(String outPutPollId);

    /**
     * 查询环境手工检测计划列表
     */
    List<EnvManualCheckPlan> selectEnvManualCheckPlanList(EnvManualCheckPlanReq req);

    /**
     * 新增环境手工检测计划
     */
    int insertEnvManualCheckPlan(EnvManualCheckPlan info);

    /**
     * 修改环境手工检测计划
     */
    int updateEnvManualCheckPlan(EnvManualCheckPlan info);

    /**
     * 删除环境手工检测计划
     */
    int deleteEnvManualCheckPlanByOutPutPollId(String outPutPollId);

}

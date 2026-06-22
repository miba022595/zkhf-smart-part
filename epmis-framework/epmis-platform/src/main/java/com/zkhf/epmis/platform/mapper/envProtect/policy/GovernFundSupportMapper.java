package com.zkhf.epmis.platform.mapper.envProtect.policy;

import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupport;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupportReq;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchActual;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchPlan;

import java.util.List;

/**
 * 政府资金支持Mapper接口
 */
public interface GovernFundSupportMapper {

    /**
     * 查询政府资金支持列表
     */
    List<GovernFundSupport> selectGovernFundSupportList(GovernFundSupportReq req);

    /**
     * 查询政府资金支持-计划列表-导出时用
     */
    List<SupportBatchPlan> selectGovernFundSupportPList(GovernFundSupportReq req);
    /**
     * 查询政府资金支持-实际列表-导出时用
     */
    List<SupportBatchActual> selectGovernFundSupportAList(GovernFundSupportReq req);

    /**
     * 查询政府资金支持-计划列表
     */
    List<SupportBatchPlan> selectSupportBatchPlanList(String supportId);

    /**
     * 查询政府资金支持-实际列表
     */
    List<SupportBatchActual> selectSupportBatchActualList(String supportId);

    /**
     * 新增政府资金支持
     */
    int insertGovernFundSupport(GovernFundSupport info);

    /**
     * 新增政府资金支持-计划批次
     */
    void insertGovernFundSupportP(SupportBatchPlan plan);

    /**
     * 新增政府资金支持-实际批次
     */
    void insertGovernFundSupportA(SupportBatchActual actual);

    /**
     * 修改政府资金支持
     */
    int updateGovernFundSupport(GovernFundSupport info);

    /**
     * 修改政府资金支持-计划批次
     */
    void updateGovernFundSupportP(SupportBatchPlan plan);

    /**
     * 修改政府资金支持-实际批次
     */
    void updateGovernFundSupportA(SupportBatchActual actual);

    /**
     * 删除政府资金支持信息
     */
    int deleteGovernFundSupportBySupportId(String supportId);

    /**
     * 删除政府资金支持信息-计划批次
     */
    void deleteGovernFundSupportPBySupportId(String supportId);

    /**
     * 删除政府资金支持信息-实际批次
     */
    void deleteGovernFundSupportABySupportId(String supportId);

    /**
     * 删除政府资金支持信息-计划批次
     */
    void deleteGovernFundSupportPById(Long id);

    /**
     * 删除政府资金支持信息-实际批次
     */
    void deleteGovernFundSupportAById(Long id);
}

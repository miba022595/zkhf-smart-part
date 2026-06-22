package com.zkhf.epmis.platform.mapper.envProtect.policy;

import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestment;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestmentReq;

import java.util.List;

/**
 * 环保法规与体系管理-环保投入Mapper接口
 */
public interface EnvInvestmentMapper {

    /**
     * 查询环保法规与体系管理-环保投入列表
     */
    long selectEnvInvestmentCount(EnvInvestmentReq req);

    /**
     * 跳页时获取跳转页的上一页最后一条id-环保投入列表
     */
    String selectSkipPageSign(EnvInvestmentReq req);

    /**
     * 查询环保法规与体系管理-环保投入列表
     */
    List<EnvInvestment> selectEnvInvestmentList(EnvInvestmentReq req);

    /**
     * 查询环保法规与体系管理-环保投入列表-导出
     */
    List<EnvInvestment> selectEnvInvestmentListForExport(EnvInvestmentReq req);

    /**
     * 新增环保法规与体系管理-环保投入
     */
    int insertEnvInvestment(EnvInvestment info);

    /**
     * 修改环保法规与体系管理-环保投入
     */
    int updateEnvInvestment(EnvInvestment info);

    /**
     * 删除环保法规与体系管理-环保投入
     */
    int deleteEnvInvestmentByInvestmentId(String investmentId);
}

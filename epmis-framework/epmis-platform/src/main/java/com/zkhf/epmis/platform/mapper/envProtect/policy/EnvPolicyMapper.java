package com.zkhf.epmis.platform.mapper.envProtect.policy;

import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyInfo;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 环境政策法规信息-Mapper接口
 */
public interface EnvPolicyMapper {

    /**
     * 查询环境政策法规列表
     */
    List<Map<String, Object>> getAllEnvPolicy(@Param("entCodes") List<String> entCodes);

    /**
     * 查询环境政策法规列表
     */
    List<EnvPolicyInfo> selectEnvPolicyList(EnvPolicyReq req);

    /**
     * 新增环境政策法规
     */
    int insertEnvPolicy(EnvPolicyInfo info);

    /**
     * 依据id查看法规信息
     */
    EnvPolicyInfo selectEnvPolicyById(String policyId);

    /**
     * 修改环境政策法规
     */
    int updateEnvPolicy(EnvPolicyInfo info);

    /**
     * 删除环境政策法规
     */
    int deleteEnvPolicyById(String policyId);
}

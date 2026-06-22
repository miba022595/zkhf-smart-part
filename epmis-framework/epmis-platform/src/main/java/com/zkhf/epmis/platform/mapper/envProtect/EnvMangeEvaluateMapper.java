package com.zkhf.epmis.platform.mapper.envProtect;

import com.zkhf.epmis.platform.envProtect.domain.EnvMangeEvaluate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;

import java.util.List;

/**
 * 企业环评环保管理-环评Mapper接口
 */
public interface EnvMangeEvaluateMapper {

    /**
     * 查询企业环评环保管理-环评列表
     */
    List<EnvMangeEvaluate> selectMangeEvaluateList(EnvMangeReq req);

    /**
     * 新增企业环评环保管理-环评
     */
    int insertMangeEvaluate(EnvMangeEvaluate info);

    /**
     * 修改企业环评环保管理-环评
     */
    int updateMangeEvaluate(EnvMangeEvaluate info);

    /**
     * 删除企业环评环保管理-环评
     */
    int deleteMangeEvaluateById(String evaluateId);
}

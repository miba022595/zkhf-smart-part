package com.zkhf.epmis.platform.mapper.envProtect;

import com.zkhf.epmis.platform.envProtect.domain.EnvMangeCheck;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;

import java.util.List;

/**
 * 企业环评环保管理-环保验收Mapper接口
 */
public interface EnvMangeCheckMapper {

    /**
     * 查询企业环评环保管理-环保验收列表
     */
    List<EnvMangeCheck> selectMangeCheckList(EnvMangeReq req);

    /**
     * 新增企业环评环保管理-环保验收
     */
    int insertMangeCheck(EnvMangeCheck info);

    /**
     * 修改企业环评环保管理-环保验收
     */
    int updateMangeCheck(EnvMangeCheck info);

    /**
     * 删除企业环评环保管理-环保验收
     */
    int deleteMangeCheckById(String checkId);
}

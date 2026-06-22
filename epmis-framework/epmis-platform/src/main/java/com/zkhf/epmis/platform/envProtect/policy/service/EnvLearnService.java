package com.zkhf.epmis.platform.envProtect.policy.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvLearn;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvLearnReq;

/**
 * 环境政策法规信息学习Service接口
 */
public interface EnvLearnService {

    /**
     * 查询环境政策法规信息学习列表
     */
    AjaxResult selectEnvLearnList(EnvLearnReq req);

    /**
     * 新增环境政策法规信息学习
     */
    AjaxResult insertEnvLearn(EnvLearn info);

    /**
     * 修改环境政策法规信息学习
     */
    AjaxResult updateEnvLearn(EnvLearn info);

    /**
     * 删除环境政策法规信息学习
     */
    AjaxResult deleteEnvLearnById(String learnId);

    /**
     * 查询学习情况统计列表
     */
    AjaxResult learnCountList(String learnId);

    /**
     * 查看学习详情列表
     */
    AjaxResult learnDetailList(String learnUserId);

    /**
     * 学习进度更新
     */
    AjaxResult learnDetailUpdate(EnvLearnReq req);

    /**
     * 依据法规文件查询管理学习个数
     */
    int checkExistsLearnByPolicyId(String policyId);

}

package com.zkhf.epmis.platform.mapper.envProtect.policy;

import com.zkhf.epmis.platform.envProtect.policy.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 环境政策法规信息学习管理Mapper接口
 */
public interface EnvLearnMapper {

    /**
     * 查询环境政策法规信息学习管理列表
     */
    List<EnvLearn> selectEnvLearnList(EnvLearnReq req);

    /**
     * 查询学习的法规
     */
    List<EnvLearnPolicy> selectEnvLearnPolicy(@Param("learnIds") List<String> learnIds);

    /**
     * 查询学习的企业
     */
    List<EnvLearnEnt> selectEnvLearnEnt(@Param("learnIds") List<String> learnIds);

    /**
     * 查询学习情况统计信息-进度
     */
    List<EnvLearnUser> selectEnvLearnUser(@Param("learnIds") List<String> learnIds);

    /**
     * 新增环境政策法规信息学习
     */
    int insertEnvLearn(EnvLearn info);

    /**
     * 新增学习政策法规
     */
    void insertEnvLearnPolicy(@Param("policyList") List<EnvLearnPolicy> policyList);

    /**
     * 新增学习企业
     */
    void insertEnvLearnEnt(@Param("entList") List<EnvLearnEnt> entList);

    /**
     * 新增学习人员
     */
    void insertEnvLearnUser(@Param("userList") List<EnvLearnUser> userList);

    /**
     * 修改环境政策法规信息学习
     */
    int updateEnvLearn(EnvLearn info);

    /**
     * 依据学习管理查询学习统计信息
     */
    List<EnvLearnUser> selectEnvLearnUserByLearnId(String learnId);

    /**
     * 删除旧学习政策法规
     */
    void deleteEnvLearnPolicy(String learnId);

    /**
     * 删除旧学习企业
     */
    void deleteEnvLearnEnt(String learnId);

    /**
     * 删除旧学习统计信息
     */
    void deleteEnvLearnUser(@Param("learnUserIds") List<String> learnUserIds);

    /**
     * 删除旧学习详情信息
     */
    void deleteEnvLearnDetail(@Param("learnUserIds") List<String> learnUserIds);

    /**
     * 删除环境政策法规信息学习
     */
    int deleteEnvLearnByLearnId(String learnId);

    /**
     * 删除学习详情信息
     */
    void deleteEnvLearnDetailByLearnId(String learnId);

    /**
     * 删除学习信息
     */
    void deleteEnvLearnUserByLearnId(String learnId);

    /**
     * 查询学习详情信息
     */
    List<LearnDetail> learnDetailList(String learnUserId);

    /**
     * 获取学习信息
     */
    EnvLearnUser getLearnUserById(String learnUserId);

    /**
     * 更新学习详情
     */
    void learnDetailUpdate(EnvLearnReq req);

    /**
     * 更新学习情况统计
     */
    void updateLearnUserCompleted(String learnUserId);

    /**
     * 依据法规文件查询管理学习个数
     */
    int checkExistsLearnByPolicyId(String policyId);
}

package com.zkhf.epmis.platform.mapper.ent;

import com.zkhf.epmis.platform.ent.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 企业排口Mapper接口
 */
public interface EntOutPutMapper {

    /**
     * 查询企业排口信息列表
     */
    List<Map<String, Object>> listAll();

    /**
     * 查询排口信息列表(包含排口位置)
     */
    List<EntOutPutPosition> listAllPosition(@Param("entCodes") List<String> entCodes);

    /**
     * 企业下排口，按排口类型分类
     */
    List<EntOutPutTreeInfo> typeTreeWithEnt(@Param("entCodes") List<String> entCodes, @Param("outPutTypes") Integer[] outPutTypes);

    /**
     * 查询企业排口列表
     */
    List<Map<String, Object>> listByEnterType(@Param("entCode") String entCode, @Param("outPutType") Integer outPutType);

    /**
     * 查询企业排口列表
     */
    List<EntOutPutInfo> selectOutPutList(EntOutPutReq req);

    /**
     * 企业排口信息
     */
    EntOutPutInfo selectOutPutById(@Param("outPutId") String outPutId);

    /**
     * 查询企业下的排口编码
     */
    List<Map<String, String>> selectOutPutCodeByEnt(@Param("entCode") String entCode);

    /**
     * 查询企业下的排口编码，排口id对于的企业下的排口
     */
    List<Map<String, String>> selectOutPutCodeFromThisId(@Param("outPutId") String outPutId);

    /**
     * 新增企业排口
     */
    int insertOutPut(EntOutPutInfo info);

    /**
     * 修改企业排口
     */
    int updateOutPut(EntOutPutInfo info);

    /**
     * 删除企业排口
     */
    int deleteOutPutById(@Param("outPutId") String outPutId);

    /**
     * 删除企业关联排口关注
     */
    void deleteOutputAtt(@Param("outPutId") String outPutId);

    /**
     * 删除企业排口对应的污染物
     */
    void deleteOutPutPoll(@Param("outPutId") String outPutId);

    /**
     * 查询污染物列表
     */
    List<Map<String, String>> selectPollutantByType(@Param("outPutType") Integer outPutType);

    /**
     * 查询企业排口的关注情况
     */
    List<Map<String, Object>> selectUserPutInfoList(@Param("userId") Long userId);

    /**
     * 获取用户关注排口个数
     */
    int userAttentionCount(Long userId);

    /**
     * 用户关注排口
     */
    void userAttentionAdd(@Param("userId") Long userId, @Param("outPutId") String outPutId);

    /**
     * 用户取消关注排口
     */
    void userAttentionDel(@Param("userId") Long userId, @Param("outPutId") String outPutId);

    /**
     * 查询用户收藏关注排口信息列表
     */
    List<Map<String, Object>> userAttentionList(Long userId);

    /**
     * 查询排口关联的污染治理设施信息
     */
    List<Map<String, Object>> selectRelatePollControlFacilityList(@Param("otherType") String otherType, @Param("outPutIds") List<String> outPutIds);

    /**
     * 查询排口关联的污染治理设施信息
     */
    void insertOutPutRelatePollControlFacility(@Param("otherType") String otherType, @Param("otherId") String otherId, @Param("facilityIds") List<String> facilityIds);

    /**
     * 删除排口和的污染治理设施关联
     */
    void deleteOutPutRelatePollControlFacilityById(@Param("otherType") String otherType, @Param("otherId") String otherId);

    /**
     * 获取所有排口Id列表
     */
    List<Map<String, Object>> outPutStatusList(@Param("entCodes") List<String> entCodes);

    /**
     * 排口报警参数查询
     */
    List<OutPutAlarmConf> selectAlarmConf(@Param("outPutId") String outPutId);

    /**
     * 排口报警参数查询
     */
    List<OutPutAlarmConf> selectAlarmPubConf(@Param("outPutId") String outPutId);

    /**
     * 删除旧的排口报警配置
     */
    void alarmConfDel(@Param("outPutId") String outPutId);

    /**
     * 排口报警配置
     */
    int alarmConfEdit(List<OutPutAlarmConf> list);
}

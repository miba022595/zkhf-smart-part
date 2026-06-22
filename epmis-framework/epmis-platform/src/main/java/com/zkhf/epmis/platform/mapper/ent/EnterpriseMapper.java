package com.zkhf.epmis.platform.mapper.ent;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.platform.ent.domain.Enterprise;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.ent.domain.EnterpriseReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 基础信息---企业基础Mapper接口
 */
public interface EnterpriseMapper {

    /**
     * 查询企业列表
     */
    List<EnterprisePart> listAll();

    /**
     * 查询企业全量列表-只要部分字段
     */
    List<EnterprisePart> selectPartListByPollType(@Param("entCodes") List<String> entCodes, @Param("pollType") Integer pollType);

    /**
     * 查询企业全量列表-只要部分字段
     */
    List<EnterprisePart> selectPartListByRegion(@Param("entCodes") List<String> entCodes, @Param("region") String region);

    /**
     * 查询企业全量列表-只要部分字段
     */
    List<EnterprisePart> selectPartListAll(@Param("entCodes") List<String> entCodes);

    /**
     * 企业下的排口树
     */
    List<Map<String, Object>> outPutTree(JSONObject req);

    /**
     * 查询企业全量列表-只要部分字段
     */
    List<EnterprisePart> outPutTreeByEnt(@Param("entCodes") List<String> entCodes, @Param("pollType") Integer pollType);

    /**
     * 查询企业全量列表-只要部分字段
     */
    List<EnterprisePart> outPutTreeByRegion(@Param("entCodes") List<String> entCodes);

    /**
     * 查询企业基础列表
     */
    List<Enterprise> selectList(EnterpriseReq req);

    /**
     * 新增企业基础
     */
    int insertEnterprise(Enterprise info);

    /**
     * 修改企业基础
     */
    int updateEnterprise(Enterprise info);

    /**
     * 查询企业基础列表-通过父级查子级列表
     */
    List<Enterprise> selectListByParentCode(String parentCode);

    /**
     * 删除企业基础
     */
    int deleteEnterpriseById(String entCode);

    /**
     * 删除企业关联排口关注
     */
    void deleteEntOutputAtt(String entCode);

    /**
     * 删除企业关联排口的污染物
     */
    void deleteEntOutputPoll(String entCode);

    /**
     * 删除企业关联排口
     */
    void deleteEntOutput(String entCode);

    /**
     * 删除企业关联用户
     */
    void deleteUserEnt(String entCode);

}

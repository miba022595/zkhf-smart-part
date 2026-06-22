package com.zkhf.epmis.platform.mapper.envProtect;

import com.zkhf.epmis.platform.envProtect.domain.EnvMangeRelate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 企业环评环保管理-项目关联环评、环保验收Mapper接口
 */
public interface EnvMangeRelateMapper {

    /**
     * 查询企业环评环保管理-项目关联环评、环保验收列表
     */
    @Select(" select 2 as relateType, c.check_id as relateId, c.check_name as relateName " +
            " from t_env_mange_relate r left join t_env_mange_check c on c.check_id = r.relate_id " +
            " where r.project_id = #{projectId} and r.relate_type = 2 " +
            " union all " +
            " select 1 as relateType, e.evaluate_id as relateId, e.evaluate_name as relateName " +
            " from t_env_mange_relate r left join t_env_mange_evaluate e on e.evaluate_id = r.relate_id " +
            " where r.project_id = #{projectId} and r.relate_type = 1 ")
    List<Map<String, Object>> selectRelateByProjectId(String projectId);

    /**
     * 查询企业环评环保管理-关联的项目列表
     */
    @Select(" select c.project_id as projectId, c.project_name as projectName " +
            " from t_env_mange_relate r left join t_env_mange_project c on c.project_id = r.project_id " +
            " where r.relate_id = #{relateId} and relate_type = #{relateType} ")
    List<Map<String, Object>> selectRelateByRelateId(String relateId, Integer relateType);

    /**
     * 移除企业环评环保管理的关联关系
     */
    @Delete("<script> " +
            "   delete from t_env_mange_relate " +
            "   <where> " +
            "       <if test='projectId != null and projectId != \"\"'> " +
            "           and project_id = #{projectId} " +
            "       </if> " +
            "       <if test='relateType != null'> " +
            "           and relate_type = #{relateType} " +
            "       </if> " +
            "       <if test='relateId != null and relateId != \"\"'> " +
            "           and relate_id = #{relateId} " +
            "       </if> " +
            "   </where> " +
            "</script> ")
    void deleteRelate(@Param("projectId") String projectId, @Param("relateType") Integer relateType, @Param("relateId") String relateId);

    /**
     * 批量添加企业环评环保管理-项目关联环评、环保验收列表
     */
    @Insert("<script> " +
            "   INSERT INTO t_env_mange_relate " +
            "       (project_id, relate_type, relate_id) " +
            "   VALUES " +
            "   <foreach item='info' collection='relateList' separator=','> " +
            "       (#{info.projectId}, #{info.relateType}, #{info.relateId})" +
            "   </foreach> " +
            "</script> ")
    void batchInsertRelate(@Param("relateList") List<EnvMangeRelate> relateList);

}
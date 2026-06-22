package com.zkhf.epmis.platform.mapper.ent;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 各配置的关联关系维护
 */
public interface BasRelateMapper {

    /**
     * 删除旧的关联关系(源方关联的目的方类型)
     */
    @Delete("<script>" +
            " delete from t_bas_relate " +
            " <where> " +
            "   source_id = #{sourceId} " +
            "   <if test='targetTypes != null and targetTypes.size() > 0'>" +
            "       and target_type in " +
            "       <foreach collection='targetTypes' item='targetType' open='(' separator=',' close=')'>" +
            "           #{targetType}" +
            "       </foreach>" +
            "   </if>" +
            " </where> " +
            " </script>")
    void deleteOldRelateBySourTarType(@Param("sourceId") String sourceId, @Param("targetTypes") List<String> targetTypes);

    /**
     * 添加新的关联关系
     */
    @Insert("<script>" +
            " insert into t_bas_relate " +
            "   (source_id, target_type, target_id) " +
            " VALUES " +
            " <foreach collection='targetList' item='item' separator=','>" +
            "   (#{sourceId}, #{item.targetType}, #{item.targetId})" +
            " </foreach>" +
            "</script>")
    void insertRelateProduceFacility(@Param("sourceId") String sourceId, @Param("targetList") List<Map<String, Object>> targetList);
}
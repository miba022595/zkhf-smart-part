package com.zkhf.epmis.process.mapper.plc;

import com.zkhf.epmis.process.plc.domain.PlcRawData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 点位信息监测Mapper接口
 */
public interface PlcMapper {

    @Select("<script>" +
            "   select id, type, data, report_time as reportTime " +
            "   from t_plc_raw_base " +
            "   <where> " +
            "       type in " +
            "       <foreach collection='types' item='type' open='(' separator=',' close=')'>" +
            "           #{type}" +
            "       </foreach> " +
            "   </where> " +
            "</script>")
    List<PlcRawData> selectRealTimeData(@Param("types") Set<String> types);

    @Select(" select table_name from information_schema.tables where table_schema = database() and table_name like #{suffix} ")
    List<String> selectTableNames(@Param("suffix") String suffix);

    /**
     * 按时间范围查询历史数据（第一页、下一页、全部）
     */
    @Select("<script>" +
            "   select id, type, data, report_time as reportTime " +
            "   from t_plc_raw_data_${suffix} " +
            "   <where> " +
            "       type in " +
            "       <foreach collection='types' item='type' open='(' separator=',' close=')'>" +
            "           #{type}" +
            "       </foreach> " +
            "       and report_time between #{start} and #{end} " +
            "       <if test='lastId != null and lastId != \"\"'> " +
            "           and id &lt; #{lastId} " +
            "       </if> " +
            "   </where> " +
            "   order by id desc " +
            "   <if test='limit != null'> " +
            "       limit #{limit}" +
            "   </if> " +
            "</script>")
    List<PlcRawData> selectHistoryDataNext(@Param("suffix") String suffix, @Param("types") Set<String> types, @Param("start") String start,
                                           @Param("end") String end, @Param("lastId") String lastId, @Param("limit") Integer limit);

    /**
     * 上一页查询
     */
    @Select("<script>" +
            "   select id, type, data, report_time as reportTime " +
            "   from t_plc_raw_data_${suffix} " +
            "   <where> " +
            "       type in " +
            "       <foreach collection='types' item='type' open='(' separator=',' close=')'>" +
            "           #{type}" +
            "       </foreach> " +
            "       and report_time between #{start} and #{end} " +
            "       <if test='firstId != null and firstId != \"\"'> " +
            "           and id &gt; #{firstId} " +
            "       </if> " +
            "   </where> " +
            "   order by id asc " +
            "   limit #{limit}" +
            "</script>")
    List<PlcRawData> selectHistoryDataPrev(@Param("suffix") String suffix, @Param("types") Set<String> types, @Param("start") String start,
                                           @Param("end") String end, @Param("firstId") String firstId, @Param("limit") int limit);
}
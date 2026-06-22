package com.zkhf.epmis.process.mapper.ai;

import com.zkhf.epmis.process.ai.domain.AiDataReq;
import com.zkhf.epmis.process.ai.domain.DataInfo;
import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * ai数据的Mapper接口
 */
public interface AiDataMapper {

    /**
     * 校验表是否存在
     */
    @Select("<script>" +
            " select table_name " +
            " from information_schema.tables " +
            " where table_schema = database() and table_name in " +
            " <foreach collection='list' item='item' open='(' separator=',' close=')'>" +
            "   #{item} " +
            " </foreach>" +
            "</script>")
    List<String> selectTableNameList(List<String> tableNames);

    /**
     * 排口在线监测数据查询
     */
    @Select("<script> " +
            " <foreach collection='tableNames' item='item' open='(' separator=' union all ' close=')'>" +
            "   select '${item}' as tableName, data_info as dataInfoStr, monitor_time as monitorTime " +
            "   from ${item} " +
            "   where data_type = #{req.dataType} and monitor_time between #{req.beginTime} and #{req.endTime} " +
            " </foreach> " +
            "</script> ")
    List<DataInfo> selectDataList(@Param("tableNames") List<String> tableNames, @Param("req") AiDataReq req);

    /**
     * 报警列表查询
     */
    @Select("<script>" +
            " select " +
            "   alarm_id as alarmId, out_put_id as outPutId, pollutant_code as pollutantCode, data_type as dataType, alarm_type as alarmType, " +
            "   start_time as startTime, null as endTime, 0 as alarmStatus, 0 as dealStatus, alarm_msg as alarmMsg " +
            " from t_data_out_warn " +
            " <where>" +
            "   <if test='outMap != null and outMap.size() > 0'>" +
            "       and out_put_id in " +
            "       <foreach index='outPutId' collection='outMap' open='(' separator=',' close=')'>" +
            "           #{outPutId}" +
            "       </foreach>" +
            "   </if>" +
            "   <if test='req.beginTime != null and req.endTime != null'>" +
            "       and start_time between #{req.beginTime} and #{req.endTime}" +
            "   </if>" +
            "   <if test='req.alarmType != null'>" +
            "       and alarm_type = #{req.alarmType}" +
            "   </if>" +
            " </where> " +
            " union all " +
            " select " +
            "   alarm_id as alarmId, out_put_id as outPutId, pollutant_code as pollutantCode, data_type as dataType, alarm_type as alarmType, " +
            "   start_time as startTime, end_time as endTime, alarm_status as alarmStatus, deal_status as dealStatus, alarm_msg as alarmMsg " +
            " from t_data_out_alarm " +
            " <where>" +
            "   <if test='outMap != null and outMap.size() > 0'>" +
            "       and out_put_id in " +
            "       <foreach index='outPutId' collection='outMap' open='(' separator=',' close=')'>" +
            "           #{outPutId}" +
            "       </foreach>" +
            "   </if>" +
            "   <if test='req.beginTime != null and req.endTime != null'>" +
            "       and start_time between #{req.beginTime} and #{req.endTime}" +
            "   </if>" +
            "   <if test='req.alarmType != null'>" +
            "       and alarm_type = #{req.alarmType}" +
            "   </if>" +
            "   <if test='req.alarmStatus != null'>" +
            "       and alarm_status = #{req.alarmStatus}" +
            "   </if>" +
            "   <if test='req.dealStatus != null'>" +
            "       and deal_status = #{req.dealStatus}" +
            "   </if>" +
            " </where> " +
            " order by alarmId desc" +
            " </script>")
    List<DurAlarmInfo> selectAlarmList(@Param("req") AiDataReq req, @Param("outMap") Map<String, OutPutInfo> outMap);
}
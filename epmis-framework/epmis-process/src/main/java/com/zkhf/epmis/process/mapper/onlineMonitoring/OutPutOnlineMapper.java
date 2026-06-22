package com.zkhf.epmis.process.mapper.onlineMonitoring;

import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.onlineMonitoring.domain.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排口在线监测Mapper接口
 */
public interface OutPutOnlineMapper {

    /**
     * 校验表是否存在
     */
    @Select(" select count(*) from information_schema.tables where table_schema = database() and table_name = #{tableName} ")
    int checkTableExistsByName(@Param("tableName") String tableName);

    /**
     * 排口在线监测列表总条数查询
     */
    @Select(" select count(*) from ${req.tableName} " +
            " where data_type = #{req.dataType} and monitor_time between #{req.start} and #{req.end} ")
    long selectDataCount(@Param("req") OutPutOnlineReq req);

    /**
     * 跳页时获取跳转页的上一页最后一条id-环保投入列表
     */
    @Select("<script> " +
            "   select " +
            "       out_id " +
            "   from ${req.tableName} " +
            "   where data_type = #{req.dataType} and monitor_time between #{req.start} and #{req.end} " +
            "   order by monitor_time desc " +
            "   limit 1 " +
            "   offset #{req.offset}" +
            "</script> ")
    String selectSkipPageSign(@Param("req") OutPutOnlineReq req);

    /**
     * 排口在线监测列表查询
     */
    @Select("<script> " +
            "   select " +
            "       out_id as outId, data_type as dataType, data_alarm as dataAlarm, data_info as dataInfoStr, monitor_time as monitorDate " +
            "       <if test='req.dataType == 1'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H:%i:%s') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 2'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H:%i') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 3'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 4'> " +
            "          , date_format(monitor_time, '%Y-%m-%d') as monitorTime " +
            "       </if>" +
            "   from ${req.tableName} " +
            "   <where> " +
            "       data_type = #{req.dataType} and monitor_time between #{req.start} and #{req.end} " +
            "       <if test='req.outPutIdF != null and req.outPutIdF != \"\"'>and out_id &gt; #{req.outPutIdF}</if> " +
            "       <if test='req.outPutIdE != null and req.outPutIdE != \"\"'>and out_id &lt; #{req.outPutIdE}</if> " +
            "   </where> " +
            "   order by monitor_time desc " +
            "   <if test='req.pageSize != null'> " +
            "       limit #{req.pageSize} " +
            "   </if>" +
            "</script> ")
    List<OutPutOnlineData> selectData(@Param("req") OutPutOnlineReq req);

    @Select(" select " +
            "   start_time as startTime, end_time as endTime, pollutant_code as pollutantCode, alarm_type as alarmType " +
            " from t_data_out_alarm " +
            " where out_put_id = #{outPutId} and start_time <= #{endTime} and (end_time is null or end_time >= #{startTime}) and data_type = #{dataType} ")
    List<DurAlarmInfo> selectDataAlarmList(@Param("outPutId") String outPutId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("dataType") Integer dataType);

    /**
     * 排口在线监测图表查询
     */
    @Select("<script> " +
            "   select " +
            "       data_info as dataInfoStr " +
            "       <if test='req.dataType == 1'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H:%i:%s') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 2'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H:%i') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 3'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 4'> " +
            "          , date_format(monitor_time, '%Y-%m-%d') as monitorTime " +
            "       </if>" +
            "   from ${req.tableName} " +
            "   where data_type = #{req.dataType} and monitor_time between #{req.start} and #{req.end} " +
            "   order by monitor_time asc " +
            "</script> ")
    List<OutPutOnlineData> selectCharDataList(@Param("req") OutPutOnlineReq req);

    /**
     * 校验表是否存在
     */
    @Select("select count(*) from information_schema.tables where table_schema = database() and table_name = #{tableName}")
    int checkExists(@Param("tableName") String tableName);

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
     * 排口在线监测列表查询
     */
    @Select("<script> " +
            " <foreach collection='outList' item='item' open='(' separator=' union all ' close=')'>" +
            "   select " +
            "       '${item.outPutId}' as outPutId, data_info as dataInfoStr " +
            "       <if test='req.dataType == 1'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H:%i:%s') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 2'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H:%i') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 3'> " +
            "          , date_format(monitor_time, '%Y-%m-%d %H') as monitorTime " +
            "       </if> " +
            "       <if test='req.dataType == 4'> " +
            "          , date_format(monitor_time, '%Y-%m-%d') as monitorTime " +
            "       </if>" +
            "   from ${item.tableName} " +
            "   where data_type = #{req.dataType} and monitor_time between #{req.start} and #{req.end} " +
            " </foreach> " +
            "</script> ")
    List<MultipleOutPutData> selectDataList(@Param("outList") List<MultipleOutPutInfo> outList, @Param("req") OutPutOnlineReq req);

    /**
     * 排口在线监测报表查询
     */
    @Select(" select " +
            "   data_info as dataInfoStr, monitor_time as monitorTime " +
            " from ${req.tableName} " +
            " where data_type = #{req.dataType} and monitor_time between #{req.start} and #{req.end}")
    List<OutPutOnlineReportData> selectReportData(@Param("req") OutPutOnlineReq req);
}
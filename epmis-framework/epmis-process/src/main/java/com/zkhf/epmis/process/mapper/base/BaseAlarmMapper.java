package com.zkhf.epmis.process.mapper.base;

import com.zkhf.epmis.process.base.domain.BaseDurAlarm;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 报警Mapper接口
 */
public interface BaseAlarmMapper {

    /**
     * 旧的预警消息清空
     */
    @Delete("<script>" +
            " delete from t_data_out_warn " +
            " <where> " +
            "   <if test='outPutId != null and outPutId != \"\"'>" +
            "       and out_put_id = #{outPutId}" +
            "   </if>" +
            "   <if test='dataType != null'> " +
            "       and data_type = #{dataType} " +
            "   </if> " +
            "   <if test='alarmTypes != null and alarmTypes.size() > 0'>" +
            "       and alarm_type in " +
            "       <foreach collection='alarmTypes' item='type' open='(' separator=',' close=')'>" +
            "           #{type}" +
            "       </foreach>" +
            "   </if>" +
            " </where> " +
            " </script>")
    void deleteOldWarn(@Param("outPutId") String outPutId, @Param("dataType") Integer dataType, @Param("alarmTypes") List<Integer> alarmTypes);

    /**
     * 排口预警数据录入
     */
    @Insert("<script> " +
            " insert into t_data_out_warn " +
            "   (alarm_id, out_put_id, pollutant_code, data_type, alarm_type, start_time, alarm_msg) " +
            " values " +
            "   <foreach item='item' collection='warnList' separator=','> " +
            "       (#{item.alarmId}, #{item.outPutId}, #{item.pollutantCode}, #{item.dataType}, #{item.alarmType}, #{item.alarmTime}, #{item.alarmMsg})" +
            "   </foreach> " +
            "</script> ")
    void batchInsertWarn(@Param("warnList") List<BaseDurAlarm> warnList);

    /**
     * 获取未解除的报警列表
     */
    @Select("<script>" +
            " select alarm_id, out_put_id, alarm_type, pollutant_code " +
            " from t_data_out_alarm" +
            " <where> " +
            "   <if test='outPutId != null'>" +
            "       and out_put_id = #{outPutId} " +
            "   </if>" +
            "   <if test='dataType != null'> " +
            "       and data_type = #{dataType} " +
            "   </if> " +
            "   <if test='alarmType != null'> " +
            "       and alarm_type = #{alarmType} " +
            "   </if> " +
            "   <if test='alarmStatus != null'>" +
            "       and alarm_status = #{alarmStatus}" +
            "   </if>" +
            " </where> " +
            " </script>")
    List<Map<String, String>> selectAlarmList(@Param("outPutId") String outPutId, @Param("dataType") Integer dataType,
                                 @Param("alarmType") Integer alarmType, @Param("alarmStatus") Integer alarmStatus);

    /**
     * 报警解除
     */
    @Update("<script> " +
            " update t_data_out_alarm " +
            " set " +
            "   alarm_status = #{alarmStatus}, deal_status = #{dealStatus}, " +
            "   end_time = " +
            "   case alarm_id " +
            "   <foreach collection='updateMap' item='endTime' index='alarmId'>" +
            "     when #{alarmId} then #{endTime} " +
            "   </foreach>" +
            "   end " +
            " where alarm_id in " +
            "   <foreach collection='updateMap.keySet()' item='alarmId' open='(' separator=',' close=')'>" +
            "     #{alarmId}" +
            "   </foreach>" +
            "</script> ")
    void batchUpdateDurAlarm(@Param("updateMap") Map<String, String> updateMap, @Param("alarmStatus") Integer alarmStatus, @Param("dealStatus") Integer dealStatus);

    /**
     * 新增报警
     */
    @Insert("<script> " +
            " insert into t_data_out_alarm " +
            "   (alarm_id, out_put_id, pollutant_code, data_type, alarm_type, start_time, alarm_status, alarm_msg) " +
            " values " +
            "   <foreach item='item' collection='alarmList' separator=','> " +
            "       (#{item.alarmId}, #{item.outPutId}, #{item.pollutantCode}, #{item.dataType}, #{item.alarmType}, #{item.alarmTime}, #{item.alarmStatus}, #{item.alarmMsg})" +
            "   </foreach> " +
            "</script> ")
    void batchInsertDurAlarm(@Param("alarmList") List<BaseDurAlarm> alarmList);
}
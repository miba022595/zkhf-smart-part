package com.zkhf.epmis.process.mapper.task;

import com.zkhf.epmis.process.task.domain.DataHourLeftEmissionInfo;
import com.zkhf.epmis.process.task.domain.EffectiveTransTask;
import com.zkhf.epmis.process.task.domain.EmissionTask;
import com.zkhf.epmis.process.task.domain.PollDataInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskHandlerMapper {

    /**
     * 获取符合要求的表名
     */
    @Select("select table_name from information_schema.tables where table_schema = database() and table_name like #{tableNameLike} ")
    List<String> selectTableName(@Param("tableNameLike") String tableNameLike);

    /**
     * 查询数据列表
     */
    @Select(" select " +
            "   out_id as outId, monitor_time as monitorTime, data_info as dataInfo " +
            " from ${tableName} " +
            " where data_type = #{dataType} and monitor_time between #{start} and #{ent} " +
            " order by monitor_time desc ")
    List<PollDataInfo> getOutPutDataList(@Param("tableName") String tableName, @Param("dataType") Integer dataType,
                                         @Param("start") LocalDateTime start, @Param("ent") LocalDateTime ent);
    
    /**
     * 年排量数据录入
     */
    @Insert("<script> " +
            " insert into t_ent_annual_emission " +
            "   (ent_code, out_put_id, emission_year, pollutant_code, emissions) " +
            " values " +
            "   <foreach item='item' collection='list' separator=','> " +
            "       (#{item.entCode}, #{item.outPutId}, #{item.emissionYear}, #{item.pollutantCode}, #{item.emissions}) " +
            "   </foreach> " +
            " on DUPLICATE key update " +
            "   emissions = values(emissions) " +
            "</script> ")
    void batchInsertOrUpdateEmissions(@Param("list") List<EmissionTask> list);

    /**
     * 清空小时剩余排放控制表
     */
    @Update("truncate table t_data_hour_left_emission_info")
    void truncateHourLeftEmission();

    /**
     * 小时剩余排放控制表录入
     */
    @Insert("<script> " +
            " insert into t_data_hour_left_emission_info " +
            "   (out_put_id, pollutant_code, standard_value, avg_value, surplus_value) " +
            " values " +
            "   <foreach item='item' collection='list' separator=','> " +
            "       (#{item.outPutId}, #{item.pollutantCode}, #{item.standardValue}, #{item.avgValue}, #{item.surplusValue}) " +
            "   </foreach> " +
            "</script> ")
    void batchInsertHourLeftEmission(@Param("list") List<DataHourLeftEmissionInfo> list);

    /**
     * 查询数据指定条件下的数据条数
     */
    @Select(" select max(monitor_time) " +
            " from ${tableName} " +
            " where data_type = #{dataType} and monitor_time >= #{start} ")
    LocalDateTime getNewestDate(@Param("tableName") String tableName, @Param("dataType") Integer dataType, @Param("start") LocalDateTime start);

    /**
     * 批量插入或更新传输数据
     */
    @Insert("<script> " +
            " insert into t_data_effective_trans " +
            "   (eff_id, out_put_id, data_type, real_trans, must_trans, monitor_time) " +
            " values " +
            "   <foreach item='item' collection='list' separator=','> " +
            "       (#{item.effId}, #{item.outPutId}, #{item.dataType}, #{item.realTrans}, #{item.mustTrans}, #{item.monitorTime}) " +
            "   </foreach> " +
            " ON DUPLICATE KEY UPDATE " +
            "   real_trans = VALUES(real_trans), " +
            "   must_trans = VALUES(must_trans)" +
            "</script> ")
    void batchInsertOrUpdateEffTrans(List<EffectiveTransTask> list);
}
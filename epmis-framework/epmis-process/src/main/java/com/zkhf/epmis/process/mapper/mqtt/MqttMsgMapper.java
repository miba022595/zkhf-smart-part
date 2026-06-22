package com.zkhf.epmis.process.mapper.mqtt;

import com.zkhf.epmis.process.mqtt.domain.MqttMsgInfo;
import com.zkhf.epmis.process.mqtt.domain.MqttPlcData;
import org.apache.ibatis.annotations.*;

/**
 * 排口在线监测Mapper接口
 */
public interface MqttMsgMapper {

    /**
     * 表不存在时添加表
     */
    @Select("create table if not exists `t_data_out_${tableSuffix}` like `t_data_out_base`")
    void createTable(@Param("tableSuffix") String tableSuffix);

    /**
     * 查看历史数据，用于更新
     */
    @Select(" select data_info from t_data_out_${tableSuffix} where  out_id = #{outId} ")
    String selectOutPollData(@Param("outId") String outId, @Param("tableSuffix") String tableSuffix);

    /**
     * 排口数据录入
     */
    @Insert(" insert into t_data_out_${tableSuffix} " +
            "   (out_id, data_type, data_alarm, monitor_time, data_info, create_time) " +
            " values " +
            "   (#{data.outId}, #{data.dataTypeInt}, #{data.dataAlarm}, #{data.monitorTime}, #{data.dataInfo}, now()) ")
    void insertOutPollData(@Param("data") MqttMsgInfo data, @Param("tableSuffix") String tableSuffix);

    /**
     * 更新数据
     */
    @Update("update t_data_out_${tableSuffix} set data_info = #{data.dataInfo}, update_time = now() where out_id = #{data.outId} ")
    void updateOutPollData(@Param("data") MqttMsgInfo data, @Param("tableSuffix") String tableSuffix);

    /**
     * 表不存在时添加表
     */
    @Select("create table if not exists `t_plc_raw_data_${tableSuffix}` like `t_plc_raw_base`")
    void createPlcDataTable(@Param("tableSuffix") String tableSuffix);

    /**
     * plc数据录入
     */
    @Insert(" insert into t_plc_raw_data_${tableSuffix} " +
            "   (id, type, data, report_time, create_time) " +
            " values " +
            "   (#{data.id}, #{data.type}, #{data.data}, #{data.reportTime}, now()) ")
    void insertPlcData(@Param("data") MqttPlcData data, @Param("tableSuffix") String tableSuffix);

    /**
     * plc数据实时数据删除
     */
    @Delete(" delete from t_plc_raw_base where type = #{type} ")
    void deletePlcRealData(String type);

    /**
     * plc数据实时数据录入
     */
    @Insert(" insert into t_plc_raw_base " +
            "   (id, type, data, report_time, create_time) " +
            " values " +
            "   (#{data.id}, #{data.type}, #{data.data}, #{data.reportTime}, now()) ")
    void insertPlcRealData(@Param("data") MqttPlcData data);
}

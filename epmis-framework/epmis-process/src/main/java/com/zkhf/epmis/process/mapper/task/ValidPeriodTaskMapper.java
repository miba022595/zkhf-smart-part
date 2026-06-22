package com.zkhf.epmis.process.mapper.task;

import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ValidPeriodTaskMapper {

    /**
     * 清空资质数据表
     */
    @Update("truncate table t_valid_period_info ")
    void truncateValidPeriodInfo();

    /**
     * 添加资质数据表
     */
    @Insert("<script> " +
            " insert into t_valid_period_info" +
            "   (ent_code, conf_type, item_id, item_name, left_days, alarm_type, alarm_rage, begin_date, end_date, send_time) " +
            " values " +
            " <foreach item='emp' collection='list' separator=','> " +
            "   (#{emp.entCode}, #{emp.confType}, #{emp.itemId}, #{emp.itemName}, #{emp.leftDays}, #{emp.alarmType}, #{emp.alarmRage}, " +
            "   #{emp.beginDate}, #{emp.endDate}, #{emp.lastSendTime}) " +
            " </foreach> " +
            "</script> ")
    void saveValidPeriodInfoList(List<ValidPeriodAlarmInfo> list);
}

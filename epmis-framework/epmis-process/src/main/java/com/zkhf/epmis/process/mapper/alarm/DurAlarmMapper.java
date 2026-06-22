package com.zkhf.epmis.process.mapper.alarm;

import com.zkhf.epmis.process.alarm.domain.DurAlarmCount;
import com.zkhf.epmis.process.alarm.domain.DurAlarmDeal;
import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.alarm.domain.DurAlarmReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报警Mapper接口
 */
public interface DurAlarmMapper {

    /**
     * 查询实时报警列表（未解除的报警列表+预警列表）
     */
    List<DurAlarmInfo> selectActiveAlarmList();

    /**
     * 报警统计
     */
    List<DurAlarmCount> countAlarm(@Param("req") DurAlarmReq req);

    /**
     * 报警列表查询
     */
    List<DurAlarmInfo> selectAlarmList(@Param("req") DurAlarmReq req);

    /**
     * 查询报警处理情况登记列表
     */
    List<DurAlarmDeal> selectAlarmDealList(String alarmId);

    /**
     * 新增报警处理
     */
    int insertAlarmDeal(@Param("alarmId") String alarmId, @Param("deal") DurAlarmDeal deal);

    /**
     * 修改报警表的处理标志
     */
    void updateAlarmDealStatus(@Param("alarmId") String alarmId, @Param("dealStatus") Integer dealStatus);

    /**
     * 删除报警处理
     */
    int deleteAlarmDealById(String dealId);
}

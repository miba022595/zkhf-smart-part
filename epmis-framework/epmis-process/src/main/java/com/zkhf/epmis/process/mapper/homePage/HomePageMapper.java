package com.zkhf.epmis.process.mapper.homePage;

import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.base.domain.OutEmissionsInfo;
import com.zkhf.epmis.process.onlineMonitoring.domain.OutPutOnlineData;
import com.zkhf.epmis.process.statistics.domain.EntEmission;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;
import java.util.Map;

/**
 * 主页的Mapper接口
 */
public interface HomePageMapper {

    /**
     * 查询排口--小时剩余排放平均值列表
     */
    List<OutEmissionsInfo> selectOutEmissionsList(@Param("outPutIdList") List<String> outPutIdList);

    /**
     * 获取符合要求的表名
     */
    List<String> selectTableName(@Param("tableNameLike") String tableNameLike);

    /**
     * 分钟数据查询
     */
    List<Map<String, Object>> selectOutMinuteList(@Param("tableNames") List<String> tableNames,
                                                  @Param("dataType") Integer dataType, @Param("limit") Integer limit,
                                                  @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 实时数据查询
     */
    List<OutPutOnlineData> selectOutRealList(@Param("tableName") String tableName,
                                                  @Param("dataType") Integer dataType, @Param("limit") Integer limit,
                                                  @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 数据查询
     */
    List<Map<String, Object>> selectOutDataList(@Param("tableNames") List<String> tableNames, @Param("orderType") String orderType,
                                                @Param("dataType") Integer dataType, @Param("limit") Integer limit,
                                                @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 报警统计
     */
    List<Map<String, Object>> selectOutAlarmCount(@Param("outPutIds") List<String> outPutIds,
                                                @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取总的小时条数，（时间用于判断是否报警）
     */
    List<Map<String, String>> selectOutDataTimeList(@Param("tableNames") List<String> tableNames, @Param("dataType") Integer dataType,
                                   @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取报警列表
     */
    List<DurAlarmInfo> selectAlarmList(@Param("outPutIds") List<String> outPutIds, @Param("dataType") Integer dataType,
                                        @Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 查询企业年排量信息记录列表
     */
    List<EntEmission> cockpitEmissions(@Param("entCode") String entCode,
                                         @Param("emissionYear") Integer emissionYear);
}

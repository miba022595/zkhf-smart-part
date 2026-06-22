package com.zkhf.epmis.process.mapper.envManual;

import com.zkhf.epmis.process.envManual.domain.EnvManualCheckReportDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 环境手工检测任务Mapper接口
 */
public interface EnvManualCheckMapper {

    /**
     * 批量插入手工检测数据
     */
    @Insert("<script>" +
            "INSERT INTO t_data_out_report " +
            "   (out_put_id, out_id, check_frequency, data_alarm, sample_date, data_info) " +
            "VALUES " +
            "<foreach item='item' collection='detailList' separator=','> " +
            "   (#{item.outPutId}, #{item.outId}, #{item.checkFrequency}, 0, #{item.sampleDate}, #{item.dataInfo})" +
            "</foreach>" +
            "ON DUPLICATE KEY UPDATE " +
            "   check_frequency = VALUES(check_frequency), " +
            "   data_alarm = 0, " +
            "   data_info = VALUES(data_info)" +
            "</script>")
    int batchInsertEnvManualCheckData(@Param("detailList") List<EnvManualCheckReportDetail> detailList);

}

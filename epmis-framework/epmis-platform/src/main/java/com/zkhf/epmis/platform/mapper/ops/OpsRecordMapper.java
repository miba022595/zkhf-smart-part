package com.zkhf.epmis.platform.mapper.ops;

import com.zkhf.epmis.platform.ops.domain.OpsRecord;
import com.zkhf.epmis.platform.ops.domain.OpsRecordReq;
import com.zkhf.epmis.platform.ops.domain.OpsStat;
import com.zkhf.epmis.platform.ops.domain.OpsTaskReq;

import java.util.List;

/**
 * 运维记录Mapper接口
 */
public interface OpsRecordMapper {

    /**
     * 查询运维记录
     */
    List<OpsRecord> selectNewestRecordDateList(OpsTaskReq req);

    /**
     * 查询运维记录
     */
    OpsRecord selectOpsRecordById(String recordId);

    /**
     * 查询运维记录列表
     */
    List<OpsRecord> selectOpsRecordList(OpsRecordReq req);

    /**
     * 新增运维记录
     */
    int insertOpsRecord(OpsRecord info);

    /**
     * 修改运维记录
     */
    int updateOpsRecord(OpsRecord info);

    /**
     * 删除运维记录
     */
    int deleteOpsRecordById(String recordId);

    /**
     * 查询运维任务统计
     */
    List<OpsStat> selectOpsTaskStat(OpsRecordReq req);

}

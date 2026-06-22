package com.zkhf.epmis.platform.mapper.ops;

import com.zkhf.epmis.platform.ops.domain.OpsAttRecord;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecordReq;
import com.zkhf.epmis.platform.ops.domain.OpsClock;

import java.util.List;

/**
 * 考勤打卡记录Mapper接口
 */
public interface OpsAttRecordMapper {

    /**
     * 查询考勤打卡记录
     */
    OpsAttRecord selectOpsAttRecordById(String recordId);

    /**
     * 查询考勤打卡记录列表
     */
    List<OpsAttRecord> selectOpsAttRecordList(OpsAttRecordReq req);

    /**
     * 查询考勤打卡记录
     * 用于判断是签到还是签退
     */
    OpsAttRecord selectNewestOpsAttRecord(OpsAttRecordReq req);

    /**
     * 新增考勤打卡记录（签到）
     */
    int opsAttPunchIn(OpsClock info);

    /**
     * 修改考勤打卡记录（签退）
     */
    int opsAttPunchOut(OpsClock info);

    /**
     * 考勤打卡校准
     */
    int calibrateOpsAttRecord(OpsAttRecord info);

    /**
     * 删除考勤打卡记录
     */
    int deleteOpsAttRecordById(String recordId);

}

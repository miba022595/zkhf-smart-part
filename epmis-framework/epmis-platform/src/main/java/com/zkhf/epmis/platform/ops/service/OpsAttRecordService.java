package com.zkhf.epmis.platform.ops.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecord;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecordReq;
import com.zkhf.epmis.platform.ops.domain.OpsClock;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 考勤打卡记录Service接口
 */
public interface OpsAttRecordService {

    /**
     * 查询考勤打卡记录列表
     */
    AjaxResult selectOpsAttRecordList(OpsAttRecordReq req);

    /**
     * 导出考勤打卡记录列表
     */
    void exportOpsAttRecord(OpsAttRecordReq req, HttpServletResponse response);

    /**
     * 获取最新一次的打卡，用于判断是签到还是签退
     */
    AjaxResult selectNewestOpsAttRecord(OpsAttRecordReq req);

    /**
     * 考勤打卡
     */
    AjaxResult opsAttClock(OpsClock info);

    /**
     * 考勤打卡协助
     */
    AjaxResult opsAttAssistant(OpsAttRecord info);

    /**
     * 删除考勤打卡记录信息
     */
    AjaxResult deleteOpsAttRecordById(String recordId);
}

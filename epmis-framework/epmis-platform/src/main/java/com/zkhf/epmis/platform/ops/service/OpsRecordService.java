package com.zkhf.epmis.platform.ops.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsRecord;
import com.zkhf.epmis.platform.ops.domain.OpsRecordReq;
import com.zkhf.epmis.platform.ops.domain.OpsTaskReq;

import java.util.List;

/**
 * 运维记录Service接口
 */
public interface OpsRecordService {

    /** 是否合格（1：合格，0：不合格） */
    Integer qualifiedS = 1;
    /** 是否合格（1：合格，0：不合格） */
    Integer qualifiedE = 0;

    /**
     * 查询运维记录列表
     */
    List<OpsRecord> getNewestRecordDateList(OpsTaskReq req);

    /**
     * 查询运维记录
     */
    AjaxResult selectOpsRecordById(String recordId);

    /**
     * 查询运维记录列表
     */
    AjaxResult selectOpsRecordList(OpsRecordReq req);

    /**
     * 新增运维记录
     */
    AjaxResult insertOpsRecord(OpsRecord info);

    /**
     * 修改运维记录
     */
    AjaxResult updateOpsRecord(OpsRecord info);

    /**
     * 删除运维记录信息
     */
    AjaxResult deleteOpsRecordById(String recordId);

    /**
     * 查询运维记录统计信息
     */
    AjaxResult selectOpsTaskStat(OpsRecordReq req);
}

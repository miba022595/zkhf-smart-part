package com.zkhf.epmis.process.enforce.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.enforce.domain.EnforceRecord;
import com.zkhf.epmis.process.enforce.domain.EnforceRecordReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 执法检查记录Service接口
 */
public interface EnforceRecordService {

    /** 检查结果是否合格（1：合格，0：不合格/异常） */
    Integer checkPass = 1;
    /** 检查结果是否合格（1：合格，0：不合格/异常） */
    Integer checkErr = 0;

    /**
     * 查询执法检查记录列表
     */
    AjaxResult selectEnforceRecordList(EnforceRecordReq req);

    /**
     * 导出执法检查记录列表
     */
    void exportEnforceRecord(EnforceRecordReq req, HttpServletResponse response);

    /**
     * 新增执法检查记录
     */
    AjaxResult insertEnforceRecord(EnforceRecord info);

    /**
     * 修改执法检查记录
     */
    AjaxResult updateEnforceRecord(EnforceRecord info);

    /**
     * 删除执法检查记录信息
     */
    AjaxResult deleteEnforceRecord(EnforceRecord info);
}

package com.zkhf.epmis.process.onlineMonitoring.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.onlineMonitoring.domain.OriginalMessReq;

/**
 * 原始报文数据-Service接口
 */
public interface OriginalMessService {

    /**
     * 原始报文数据列表查询
     */
    AjaxResult selectList(OriginalMessReq req);
}

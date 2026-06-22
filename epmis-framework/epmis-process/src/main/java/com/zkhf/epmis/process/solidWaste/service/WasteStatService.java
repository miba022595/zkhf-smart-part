package com.zkhf.epmis.process.solidWaste.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteLibReq;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 固废统计汇总Service接口
 */
public interface WasteStatService {

    /** 流转处理量保留精度 */
    Integer dataScale = 3;

    /**
     * 查询固废总量统计汇总列表(总台账)
     */
    AjaxResult selectStatList(WasteLibReq req);

    /**
     * 导出固废总量统计汇总列表
     */
    void exportStat(WasteLibReq req, HttpServletResponse response);
}

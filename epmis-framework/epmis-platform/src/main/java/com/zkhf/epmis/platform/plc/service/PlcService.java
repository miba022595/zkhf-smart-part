package com.zkhf.epmis.platform.plc.service;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.plc.domain.PlcInfo;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 企业PLC设备点位信息Service接口
 */
public interface PlcService {

    /** 状态(0:禁用 1:启用) */
    Integer disable = 0;
    /** 状态(0:禁用 1:启用) */
    Integer enable = 1;

    /**
     * 查询企业PLC设备点位列表
     */
    List<PlcInfo> plcPointList(String entCode);

    /**
     * 查询企业PLC设备点位列表
     */
    AjaxResult selectEntPlcList(JSONObject req);

    /**
     * 导出企业PLC设备点位列表
     */
    void exportEntPlcList(JSONObject req, HttpServletResponse response);

    /**
     * 更新企业PLC设备单个点位
     */
    AjaxResult updateEntPlc(PlcInfo plc);

    /**
     * 更新企业的PLC设备点位
     */
    AjaxResult updateEntPlc(JSONObject req);
}

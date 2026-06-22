package com.zkhf.epmis.process.solidWaste.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoom;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoomReq;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 固废间管理Service接口
 * 危废必须做，一般固废必须做，生活垃圾配设施
 */
public interface WasteRoomService {
    /** 是否安装摄像头（0否 1是） */
    Integer hasSign = 1;

    /** 数据精度，保留3位小数 */
    Integer dataScale = 3;

    /**
     * 查询固废间管理列表
     */
    AjaxResult selectWasteRoomList(WasteRoomReq req);

    /**
     * 导出固废间管理列表
     */
    void exportWasteRoom(WasteRoomReq req, HttpServletResponse response);

    /**
     * 新增固废间管理
     */
    AjaxResult insertWasteRoom(WasteRoom info);

    /**
     * 修改固废间管理
     */
    AjaxResult updateWasteRoom(WasteRoom info);

    /**
     * 删除固废间管理信息
     */
    AjaxResult deleteWasteRoom(WasteRoom info);
}

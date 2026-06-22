package com.zkhf.epmis.process.solidWaste.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoom;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoomReq;
import com.zkhf.epmis.process.solidWaste.service.WasteRoomService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 固废间管理Controller
 */
@RestController
@RequestMapping("/process/wasteRoom")
public class WasteRoomController {

    private WasteRoomService wasteRoomService;
    @Autowired
    public void setWasteRoomService(WasteRoomService wasteRoomService) {
        this.wasteRoomService = wasteRoomService;
    }

    /**
     * 查询固废间管理列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) WasteRoomReq req) {
        return wasteRoomService.selectWasteRoomList(req);
    }

    /**
     * 导出固废间管理列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) WasteRoomReq req, HttpServletResponse response) {
        wasteRoomService.exportWasteRoom(req, response);
    }

    /**
     * 新增固废间管理
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody(required = false) WasteRoom info) {
        return wasteRoomService.insertWasteRoom(info);
    }

    /**
     * 修改固废间管理
     */
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody(required = false) WasteRoom info) {
        return wasteRoomService.updateWasteRoom(info);
    }

    /**
     * 删除固废间管理
     */
    @PostMapping("/remove")
    public AjaxResult remove(@RequestBody(required = false) WasteRoom info) {
        return wasteRoomService.deleteWasteRoom(info);
    }
}

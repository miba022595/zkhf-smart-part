package com.zkhf.epmis.process.solidWaste.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.service.WasteDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 固废分类字典Controller
 */
@RestController
@RequestMapping("/process/wasteDict")
public class WasteDictController {

    private WasteDictService wasteDictService;
    @Autowired
    public void setWasteDictService(WasteDictService wasteDictService) {
        this.wasteDictService = wasteDictService;
    }

    /**
     * 查询固废分类字典列表
     * pid有值时只查pid的子级列表
     */
    @GetMapping("/list")
    public AjaxResult list(Long pid) {
        return AjaxResult.success(wasteDictService.selectWasteDictList(pid));
    }
}

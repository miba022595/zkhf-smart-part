package com.zkhf.epmis.platform.base.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.ValidPeriodConf;
import com.zkhf.epmis.platform.base.service.ValidPeriodConfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)Controller
 */
@RestController
@RequestMapping("/platform/base/validPeriodConf")
public class ValidPeriodConfController {

    private ValidPeriodConfService validPeriodConfService;
    @Autowired
    public void setValidPeriodConfService(ValidPeriodConfService validPeriodConfService) {
        this.validPeriodConfService = validPeriodConfService;
    }

    /**
     * 查询企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)列表
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String entCode) {
        return validPeriodConfService.selectValidPeriodConfList(entCode);
    }

    /**
     * 修改企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)
     */
    @PutMapping
    public AjaxResult edit(@RequestBody ValidPeriodConf info) {
        return validPeriodConfService.updateValidPeriodConf(info);
    }
}

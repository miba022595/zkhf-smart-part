package com.zkhf.epmis.platform.envFee.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.service.EnvFeeDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环保费用登记各类型字典获取 Controller
 */
@RestController
@RequestMapping("/platform/fees")
public class EnvFeeDictController {

    private EnvFeeDictService envFeeDictService;
    @Autowired
    public void setEnvFeeDictService(EnvFeeDictService envFeeDictService) {
        this.envFeeDictService = envFeeDictService;
    }

    /**
     * 获取所有费用类型信息
     */
    @GetMapping("/allFeeType")
    public AjaxResult allFeeType() {
        return AjaxResult.success(envFeeDictService.selectAllFeeType());
    }

    /**
     * 获取所有缴费状态信息
     */
    @GetMapping("/allFeeStatus")
    public AjaxResult allFeeStatus() {
        return AjaxResult.success(envFeeDictService.selectAllFeeStatus());
    }

    /**
     * 获取所有支付方式信息
     */
    @GetMapping("/allPaymentMethod")
    public AjaxResult allPaymentMethod() {
        return AjaxResult.success(envFeeDictService.selectAllPaymentMethod());
    }

    /**
     * 获取所有付款状态信息
     */
    @GetMapping("/allPaymentStatus")
    public AjaxResult allPaymentStatus() {
        return AjaxResult.success(envFeeDictService.selectAllPaymentStatus());
    }

    /**
     * 获取所有发票类型信息
     */
    @GetMapping("/allInvoiceType")
    public AjaxResult allInvoiceType() {
        return AjaxResult.success(envFeeDictService.selectAllInvoiceType());
    }
}

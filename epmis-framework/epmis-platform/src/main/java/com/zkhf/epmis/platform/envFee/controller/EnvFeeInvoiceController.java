package com.zkhf.epmis.platform.envFee.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoice;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeInvoiceReq;
import com.zkhf.epmis.platform.envFee.service.EnvFeeInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 费用发票信息Controller
 */
@RestController
@RequestMapping("/platform/fees/invoice")
public class EnvFeeInvoiceController {

    private EnvFeeInvoiceService envFeeInvoiceService;
    @Autowired
    public void setEnvFeeInvoicesService(EnvFeeInvoiceService envFeeInvoiceService) {
        this.envFeeInvoiceService = envFeeInvoiceService;
    }

    /**
     * 查询环保费用登记下的发票列表
     */
    @GetMapping("/listByFeeId/{feeId}")
    public AjaxResult listByFeeId(@PathVariable String feeId) {
        return envFeeInvoiceService.selectEnvFeeInvoiceListByFeeId(feeId);
    }

    /**
     * 查询费用发票信息列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvFeeInvoiceReq req) {
        return envFeeInvoiceService.selectEnvFeeInvoiceList(req);
    }

    /**
     * 导出费用发票信息列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnvFeeInvoiceReq req, HttpServletResponse response) {
        envFeeInvoiceService.exportEnvFeeInvoice(req, response);
    }

    /**
     * 新增费用发票信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvFeeInvoice info) {
        return envFeeInvoiceService.insertEnvFeeInvoice(info);
    }

    /**
     * 修改费用发票信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvFeeInvoice info) {
        return envFeeInvoiceService.updateEnvFeeInvoice(info);
    }

    /**
     * 删除费用发票信息
     */
    @DeleteMapping("/{invoiceNumber}")
    public AjaxResult remove(@PathVariable String invoiceNumber) {
        return envFeeInvoiceService.deleteEnvFeeInvoiceById(invoiceNumber);
    }
}

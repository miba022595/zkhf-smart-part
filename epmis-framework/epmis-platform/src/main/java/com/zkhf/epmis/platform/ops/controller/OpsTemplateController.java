package com.zkhf.epmis.platform.ops.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsTemplate;
import com.zkhf.epmis.platform.ops.domain.OpsTemplateReq;
import com.zkhf.epmis.platform.ops.service.OpsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 运维模板配置Controller
 */
@RestController
@RequestMapping("/platform/ops/template")
public class OpsTemplateController {

    private OpsTemplateService opsTemplateService;
    @Autowired
    public void setOpsTemplateService(OpsTemplateService opsTemplateService) {
        this.opsTemplateService = opsTemplateService;
    }

    /**
     * 获取运维类型信息
     */
    @GetMapping(value = "/allType")
    public AjaxResult allTemplateType(@RequestParam(required = false) String outPutId) {
        return opsTemplateService.allTemplateType(outPutId);
    }

    /**
     * 获取运维模板详细信息
     * 获取企业排口的运维模板，私有 > 全局公共
     */
    @GetMapping(value = "/getInfo")
    public AjaxResult getInfo(@RequestParam(required = false) String entCode,
                              @RequestParam(required = false) String outPutId,
                              @RequestParam(required = false) String templateCode) {
        OpsTemplate info = opsTemplateService.selectOpsTemplateDetail(entCode, outPutId, templateCode);
        return AjaxResult.success(info);
    }

    /**
     * 查询运维模板列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) OpsTemplateReq req) {
        return opsTemplateService.selectOpsTemplateList(req);
    }

    /**
     * 新增运维模板
     */
    @PostMapping
    public AjaxResult add(@RequestBody OpsTemplate info) {
        return opsTemplateService.insertOpsTemplate(info);
    }

    /**
     * 修改运维模板
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OpsTemplate info) {
        return opsTemplateService.updateOpsTemplate(info);
    }

    /**
     * 删除运维模板-相当于恢复成公共的模板
     */
    @DeleteMapping
    public AjaxResult remove(@RequestParam(required = false) String entCode,
                             @RequestParam(required = false) String outPutId,
                             @RequestParam(required = false) String templateCode) {
        return opsTemplateService.deleteOpsTemplate(entCode, outPutId, templateCode);
    }
}


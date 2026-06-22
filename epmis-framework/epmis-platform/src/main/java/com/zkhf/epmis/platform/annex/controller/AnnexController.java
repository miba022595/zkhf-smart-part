package com.zkhf.epmis.platform.annex.controller;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件处理
 */
@RestController
@RequestMapping("/platform/annex")
public class AnnexController {

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    /**
     * 获取附件列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) AnnexReq req) {
        return AjaxResult.success(annexService.selectAnnexList(req));
    }

    /**
     * 依据附件归属获取附件列表
     */
    @GetMapping("/getBySource/{sourceType}/{sourceId}")
    public AjaxResult list(@PathVariable("sourceType") String sourceType, @PathVariable("sourceId") String sourceId) {
        return AjaxResult.success(annexService.selectAnnexList(sourceId, sourceType));
    }

    /**
     * 上传附件
     * 添加文件时便指定文件归属 sourceType
     */
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, @RequestParam("sourceType") String sourceType) {
        return annexService.insertAnnex(file, sourceType);
    }

    /**
     * 更新附件
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody(required = false) JSONObject annexInfo) {
        return annexService.updateAnnex(annexInfo);
    }

    /**
     * 删除附件
     */
    @DeleteMapping("/delete/{ids}")
    public AjaxResult delete(@PathVariable List<String> ids) {
        return annexService.deleteAnnex(ids);
    }
}

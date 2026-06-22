package com.zkhf.epmis.platform.ent.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.OutPutAlarmConf;
import com.zkhf.epmis.platform.ent.domain.EntOutPutInfo;
import com.zkhf.epmis.platform.ent.domain.EntOutPutReq;
import com.zkhf.epmis.platform.ent.service.EntOutPutInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 企业排口信息Controller
 */
@RestController
@RequestMapping("/platform/ent/outPutInfo")
public class EntOutPutInfoController {

    private EntOutPutInfoService entOutPutInfoService;

    @Autowired
    public void setEntOutPutInfoService(EntOutPutInfoService entOutPutInfoService) {
        this.entOutPutInfoService = entOutPutInfoService;
    }

    /**
     * 查询排口信息列表(包含排口位置)
     */
    @GetMapping("/listAllPosition")
    public AjaxResult listAllPosition() {
        return AjaxResult.success(entOutPutInfoService.listAllPosition());
    }

    /**
     * 企业下排口，按排口类型分类
     */
    @GetMapping("/typeTreeWithEnt")
    public AjaxResult typeTreeWithEnt(Integer[] outPutTypes) {
        return entOutPutInfoService.typeTreeWithEnt(outPutTypes);
    }

    /**
     * 依据排口类型查询企业下的排口
     */
    @GetMapping("/listByEnterType/{entCode}/{outPutType}")
    public AjaxResult listByEnterType(@PathVariable String entCode, @PathVariable Integer outPutType) {
        return AjaxResult.success(entOutPutInfoService.listByEnterType(entCode, outPutType));
    }

    /**
     * 查询企业排口详细信息
     */
    @GetMapping("/{outPutId}")
    public AjaxResult detail(@PathVariable("outPutId") String outPutId) {
        return entOutPutInfoService.selectOutPutById(outPutId);
    }

    /**
     * 查询企业排口列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EntOutPutReq req) {
        return entOutPutInfoService.selectOutPutList(req);
    }

    /**
     * 导出企业排口列表
     * 按模板导出
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EntOutPutReq req, HttpServletResponse response) {
        entOutPutInfoService.exportOutPut(req, response);
    }

    /**
     * 新增企业排口
     */
    @PostMapping
    public AjaxResult add(@RequestBody EntOutPutInfo info) {
        return entOutPutInfoService.insertOutPut(info);
    }

    /**
     * 修改企业排口
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EntOutPutInfo info) {
        return entOutPutInfoService.updateOutPut(info);
    }

    /**
     * 删除企业排口
     */
    @DeleteMapping("/{outPutId}")
    public AjaxResult remove(@PathVariable String outPutId) {
        return entOutPutInfoService.deleteOutPutById(outPutId);
    }

    /**
     * 用户关注排口
     */
    @GetMapping("/userAttentionAdd")
    public AjaxResult userAttentionAdd(String outPutId) {
        return entOutPutInfoService.userAttentionAdd(outPutId);
    }

    /**
     * 用户取消关注排口
     */
    @GetMapping("/userAttentionDel")
    public AjaxResult userAttentionDel(String outPutId) {
        return entOutPutInfoService.userAttentionDel(outPutId);
    }

    /**
     * 排口报警参数查询
     */
    @GetMapping("/alarmConf")
    public AjaxResult selectAlarmConf(@RequestParam(required = false) String outPutId) {
        return entOutPutInfoService.selectAlarmConf(outPutId);
    }

    /**
     * 排口报警配置
     */
    @PutMapping("/alarmConf")
    public AjaxResult alarmConfEdit(@RequestBody List<OutPutAlarmConf> list) {
        return entOutPutInfoService.alarmConfEdit(list);
    }
}

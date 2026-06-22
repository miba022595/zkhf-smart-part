package com.zkhf.epmis.platform.ent.controller;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.Enterprise;
import com.zkhf.epmis.platform.ent.domain.EnterpriseReq;
import com.zkhf.epmis.platform.ent.service.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 企业基础Controller
 */
@RestController
@RequestMapping("/platform/enterprise")
public class EnterpriseController {

    private EnterpriseService enterpriseService;
    @Autowired
    public void setEnterpriseService(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    /**
     * 查询企业列表-排口类型筛选
     * pollType 排放口类型，1废水、2废气、3无组织、4VOC
     */
    @GetMapping("/listByPollType/{pollType}")
    public AjaxResult listByPollType(@PathVariable Integer pollType) {
        return enterpriseService.listByPollType(pollType);
    }

    /**
     * 查询企业列表-企业所属地区筛选
     * region 所属企业，如1,11,1101....，向下匹配
     */
    @GetMapping("/listByRegion")
    public AjaxResult listByRegion(@RequestParam(required = false) String region) {
        return enterpriseService.listByRegion(region);
    }

    /**
     * 查询企业全量列表-只要部分字段
     */
    @GetMapping("/partListAll")
    public AjaxResult partListAll() {
        return enterpriseService.selectPartListAll();
    }


    /**
     * 企业下的排口树
     */
    @PostMapping("/outPutTree")
    public AjaxResult outPutTree(@RequestBody(required = false) JSONObject req) {
        return enterpriseService.outPutTree(req);
    }

    /**
     * 企业下的排口树-按企业分层级
     * pollType 排放口类型，1 废水、2 废气。。。
     */
    @GetMapping("/outPutTreeByEnt")
    public AjaxResult outPutTreeByEnt(@RequestParam(required = false) Integer pollType) {
        return enterpriseService.outPutTreeByEnt(pollType);
    }

    /**
     * 企业下的排口树-按所属地区分层级
     */
    @GetMapping("/outPutTreeByRegion")
    public AjaxResult outPutTreeByRegion() {
        return enterpriseService.outPutTreeByRegion();
    }


    /**
     * 查询基础信息---企业基础列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnterpriseReq req) {
        return enterpriseService.selectList(req);
    }

    /**
     * 导出企业基础列表
     */
    @PostMapping("/exportTemplate")
    public void exportTemplate(@RequestBody(required = false) EnterpriseReq req, HttpServletResponse response) {
        enterpriseService.exportList(req, response);
    }

    /**
     * 新增基础信息---企业基础
     */
    @PostMapping
    public AjaxResult add(@RequestBody Enterprise info) {
        return enterpriseService.insertEnterprise(info);
    }

    /**
     * 修改基础信息---企业基础
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Enterprise info) {
        return enterpriseService.updateEnterprise(info);
    }

    /**
     * 删除基础信息---企业基础
     */
    @DeleteMapping("/{entCode}")
    public AjaxResult remove(@PathVariable String entCode) {
        return enterpriseService.deleteEnterpriseByEntCode(entCode);
    }

    /**
     * 单个企业排污许可信息爬取
     */
    @GetMapping("/entSpider/{entCode}")
    public AjaxResult entLicenseSpider(@PathVariable String entCode) {
        return enterpriseService.entLicenseSpider(entCode);
    }
}

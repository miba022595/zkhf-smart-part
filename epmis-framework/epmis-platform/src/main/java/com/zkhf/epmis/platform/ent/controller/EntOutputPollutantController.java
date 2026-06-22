package com.zkhf.epmis.platform.ent.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.EntOutputPollutant;
import com.zkhf.epmis.platform.ent.service.EntOutputPollutantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业排口污染物信息Controller
 */
@RestController
@RequestMapping("/platform/ent/outPutPollutant")
public class EntOutputPollutantController {

    private EntOutputPollutantService entOutputPollutantService;

    @Autowired
    public void setEntOutputPollutantService(EntOutputPollutantService entOutputPollutantService) {
        this.entOutputPollutantService = entOutputPollutantService;
    }

    /**
     * 通过企业排口id查询对应的污染物信息
     */
    @GetMapping("/outPut/{outPutId}")
    public AjaxResult getOutputPollutant(@PathVariable("outPutId") String outPutId) {
        return AjaxResult.success(entOutputPollutantService.selectOutputPollutantByOutPutId(outPutId));
    }

    /**
     * 排口自动表头列表
     */
    @GetMapping("/getAutoHead")
    public AjaxResult getAutoHead(String outPutId, String dataEnum) {
        return AjaxResult.success(entOutputPollutantService.selectAutoHead(outPutId, dataEnum));
    }

    /**
     * 排口自动表头列表-多排口
     * 取公共的
     */
    @GetMapping("/multiple/autoHead")
    public AjaxResult multipleAutoHead(@RequestParam("outPutIds") List<String> outPutIds,
                                       @RequestParam("dataEnum") String dataEnum) {
        return AjaxResult.success(entOutputPollutantService.multipleAutoHead(outPutIds, dataEnum));
    }

    /**
     * 排口自动表头列表-多排口
     * 取公共的
     */
    @GetMapping("/multiple/autoHeads")
    public AjaxResult multipleAutoHeads(@RequestParam("outPutIds") List<String> outPutIds,
                                       @RequestParam("dataEnum") String dataEnum) {
        return AjaxResult.success(entOutputPollutantService.multipleAutoHeads(outPutIds, dataEnum));
    }

    /**
     * 通过企业排口的污染物id查询对应的污染物信息
     */
    @GetMapping("/{outPutPollId}")
    public AjaxResult getInfo(@PathVariable("outPutPollId") String outPutPollId) {
        return entOutputPollutantService.selectOutputPollutantById(outPutPollId);
    }

    /**
     * 新增企业排口污染物信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody EntOutputPollutant poll) {
        return entOutputPollutantService.insertOutputPollutant(poll);
    }

    /**
     * 修改企业排口污染物信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EntOutputPollutant poll) {
        return entOutputPollutantService.updateOutputPollutant(poll);
    }

    /**
     * 删除企业排口污染物信息
     */
    @DeleteMapping("/{outPutPollId}")
    public AjaxResult remove(@PathVariable String outPutPollId) {
        return entOutputPollutantService.deleteOutputPollutantById(outPutPollId);
    }
}
package com.zkhf.epmis.platform.envProtect.policy.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvLearn;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvLearnReq;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvLearnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 环境政策法规信息学习管理Controller
 */
@RestController
@RequestMapping("/platform/envLearn")
public class EnvLearnController {

    private EnvLearnService envLearnService;
    @Autowired
    public void setEnvLearnService(EnvLearnService envLearnService) {
        this.envLearnService = envLearnService;
    }

    /**
     * 查询环境政策法规信息学习列表
     */
    @PostMapping("/list")
    public AjaxResult list(@RequestBody(required = false) EnvLearnReq req) {
        return envLearnService.selectEnvLearnList(req);
    }

    /**
     * 新增环境政策法规信息学习
     */
    @PostMapping
    public AjaxResult add(@RequestBody EnvLearn info) {
        return envLearnService.insertEnvLearn(info);
    }

    /**
     * 修改环境政策法规信息学习
     */
    @PutMapping
    public AjaxResult edit(@RequestBody EnvLearn info) {
        return envLearnService.updateEnvLearn(info);
    }

    /**
     * 删除环境政策法规信息学习
     */
	@DeleteMapping("/{learnId}")
    public AjaxResult remove(@PathVariable String learnId) {
        return envLearnService.deleteEnvLearnById(learnId);
    }

    /**
     * 查询学习情况统计列表
     */
    @GetMapping("/learnCountList/{learnId}")
    public AjaxResult learnCountList(@PathVariable String learnId) {
        return envLearnService.learnCountList(learnId);
    }

    /**
     * 查看学习详情列表
     */
    @GetMapping("/learnDetailList/{learnUserId}")
    public AjaxResult learnDetailList(@PathVariable String learnUserId) {
        return envLearnService.learnDetailList(learnUserId);
    }

    /**
     * 学习进度更新
     */
    @PutMapping("/learnDetail")
    public AjaxResult learnDetailUpdate(@RequestBody EnvLearnReq req) {
        return envLearnService.learnDetailUpdate(req);
    }
}

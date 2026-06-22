package com.zkhf.epmis.process.ai.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.ai.domain.AiDataReq;
import com.zkhf.epmis.process.ai.service.AiDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ai数据获取的 Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/aiData")
public class AiDataController {

    private AiDataService aiDataService;
    @Autowired
    public void setOutPutOnlineService(AiDataService aiDataService) {
        this.aiDataService = aiDataService;
    }

    /**
     * 获取数据列表
     */
    @PostMapping("/onlineMonitorList")
    public AjaxResult onlineMonitorList(@RequestBody(required = false) AiDataReq req) {
        return aiDataService.onlineMonitorList(req);
    }

    /**
     * 获取数据列表-返回精简的文本格式
     */
    @PostMapping("/onlineMonitorListText")
    public String onlineMonitorListText(@RequestBody(required = false) AiDataReq req) {
        return aiDataService.onlineMonitorListText(req);
    }

    /**
     * 获取报警列表
     */
    @PostMapping("/alarmList")
    public AjaxResult alarmList(@RequestBody(required = false) AiDataReq req) {
        return aiDataService.selectAlarmList(req);
    }
}

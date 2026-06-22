package com.zkhf.epmis.process.send.test;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.send.robotPhone.AliYunVoicePush;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试 Controller
 */
@Slf4j
@RestController
@RequestMapping("/process/test")
public class TestController {

    @Autowired
    private AliYunVoicePush voiceService;

    /**
     * 发送语音通知
     */
    @PostMapping("/send")
    public AjaxResult sendVoice(@RequestParam String phoneNumber, @RequestParam String text) {
        voiceService.textToVoice(phoneNumber, text);
        return AjaxResult.success();
    }
}

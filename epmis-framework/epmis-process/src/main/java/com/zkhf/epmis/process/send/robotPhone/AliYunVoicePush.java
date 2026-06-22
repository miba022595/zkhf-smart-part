package com.zkhf.epmis.process.send.robotPhone;

import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dyvmsapi.model.v20170525.SingleCallByTtsRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.zkhf.epmis.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 使用阿里云语音功能实现机器人电话通知
 */
@Slf4j
@Component
public class AliYunVoicePush {

    @Value("${aliyun.open:false}")
    private boolean isOpen;

    @Value("${aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.region-id:cn-hangzhou}")
    private String regionId;

    @Value("${aliyun.voice.tts-template-id:TTS_XXXXXX}")
    private String ttsTemplateId;

    @Value("${aliyun.voice.called-show-number:0571XXXXXXX}")
    private String calledShowNumber;

    private IAcsClient acsClient = null;

    /**
     * 通用文字转语音播出
     * 通过动态模板参数实现任意文本转语音
     * 也可以发送自定义格式通知
     * 如：
     *      会议提醒：String.format(您有会议%s将于%s在%s召开，请准时参加。, meetingTitle, meetingTime, location)
     *      天气预警：String.format(%s今天天气%s，温度%s度。温馨提示：%s。, city, weather, temperature, tips)
     */
    @Async
    public void textToVoice(String phoneNumber, String text) {
        if (StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(text)) {
            log.error("发送的信息为空 phoneNumber {}， text {}", phoneNumber, text);
            return;
        }
        // 使用通用模板，将整个文本作为content参数
        JSONObject json = new JSONObject();
        json.put("content", text);
        sendVoiceCall(phoneNumber, json);
    }

    /**
     * 发送语音呼叫的核心方法
     */
    private void sendVoiceCall(String phoneNumber, JSONObject json) {
        if (!isOpen) { // 不开启时直接返回
            log.error("发送功能未开启，请确认");
            return;
        }
        try {
            if (acsClient == null) {
                acsClient = new DefaultAcsClient(DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret));
            }
            SingleCallByTtsRequest request = new SingleCallByTtsRequest();
            request.setCalledNumber(phoneNumber);
            request.setTtsCode(ttsTemplateId);
            request.setTtsParam(json.toJSONString());
            request.setCalledShowNumber(calledShowNumber);
            request.setPlayTimes(2);
            request.setVolume(100);

            log.info("发送文字转语音: 号码={}, 参数={}", phoneNumber, json);

            acsClient.getAcsResponse(request);

            log.info("语音通知发送完成");

        } catch (ClientException e) {
            log.error("文字转语音API调用失败: {}", e.getErrMsg());
        }
    }
}
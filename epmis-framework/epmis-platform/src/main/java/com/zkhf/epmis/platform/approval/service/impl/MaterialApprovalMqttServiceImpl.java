package com.zkhf.epmis.platform.approval.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstance;
import com.zkhf.epmis.platform.approval.domain.ApprovalResultMqttMessage;
import com.zkhf.epmis.platform.approval.enums.ApprovalInstanceStatus;
import com.zkhf.epmis.platform.approval.service.ApprovalService;
import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mqtt.gateway.MqttPublishGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service("materialApprovalMqttService")
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
public class MaterialApprovalMqttServiceImpl implements ApprovalService {

    @Value("${mqtt.topics.approval-result:approval_result}")
    private String topicApprovalResult;

    private MqttPublishGateway mqttPublishGateway;

    @Autowired(required = false)
    public void setMqttPublishGateway(MqttPublishGateway mqttPublishGateway) {
        this.mqttPublishGateway = mqttPublishGateway;
    }

    @Override
    public void approval(ApprovalInstance instance) {
        if (instance == null) {
            return;
        }
        if (!ApprovalInstanceStatus.PROCESSING.code.equals(instance.getStatus())
                && !ApprovalInstanceStatus.APPROVED.code.equals(instance.getStatus())
                && !ApprovalInstanceStatus.REJECTED.code.equals(instance.getStatus())
                && !ApprovalInstanceStatus.CANCELLED.code.equals(instance.getStatus())) {
            return;
        }
        if (mqttPublishGateway == null) {
            return;
        }
        ApprovalResultMqttMessage msg = new ApprovalResultMqttMessage();
        msg.setBusinessType(instance.getBusinessType());
        msg.setBusinessKey(instance.getBusinessKey());
        msg.setEntCode(instance.getEntCode());
        msg.setStatus(instance.getStatus());
        msg.setComment(instance.getComment());
        LoginUser user = GVarContainer.getLoginUser();
        if (user != null) {
            msg.setActionUserId(user.getUserId());
            msg.setActionUserName(GVarContainer.getUserName());
        }
        msg.setActionTime(LocalDateTime.now());
        try {
            mqttPublishGateway.publish(JSONObject.toJSONString(msg), topicApprovalResult, 1);
        } catch (Exception e) {
            log.error("publish approval result mqtt failed: businessType {} businessKey {}", instance.getBusinessType(), instance.getBusinessKey(), e);
        }
    }
}

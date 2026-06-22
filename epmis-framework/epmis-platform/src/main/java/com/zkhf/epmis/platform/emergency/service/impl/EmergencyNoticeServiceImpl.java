package com.zkhf.epmis.platform.emergency.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNotice;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNoticeReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyNoticeService;
import com.zkhf.epmis.platform.ent.domain.Enterprise;
import com.zkhf.epmis.platform.ent.domain.EnterpriseReq;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.emergency.EmergencyNoticeMapper;
import com.zkhf.epmis.platform.mapper.ent.EnterpriseMapper;
import com.zkhf.epmis.platform.send.weixin.WeComSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmergencyNoticeServiceImpl implements EmergencyNoticeService {

    private static final int FULL_NICKNAME_QUERY_THRESHOLD = 100;

    private EmergencyNoticeMapper emergencyNoticeMapper;
    @Autowired
    public void setEmergencyNoticeMapper(EmergencyNoticeMapper emergencyNoticeMapper) {
        this.emergencyNoticeMapper = emergencyNoticeMapper;
    }

    private EnterpriseMapper enterpriseMapper;
    @Autowired
    public void setEnterpriseMapper(EnterpriseMapper enterpriseMapper) {
        this.enterpriseMapper = enterpriseMapper;
    }

    private WeComSend weComSend;
    @Autowired
    public void setWeComSend(WeComSend weComSend) {
        this.weComSend = weComSend;
    }

    @Override
    public AjaxResult list(EmergencyNoticeReq req) {
        // 请求参数转换
        req = initEmergencyNoticeReq(req);
        if (null == req) {
            return AjaxResult.success();
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EmergencyNotice> list = emergencyNoticeMapper.selectList(req);
        replaceReceiverNamesWithNickNames(list);
        return PageUtils.getAjaxResult(list, page);
    }

    private EmergencyNoticeReq initEmergencyNoticeReq(EmergencyNoticeReq req) {
        if (null == req) {
            req = new EmergencyNoticeReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            List<String> authEntCodes = GVarContainer.getEntCodes();
            if (authEntCodes.isEmpty()) {
                return null;
            }
            if (StringUtils.isNotBlank(req.getEntCode())) {
                if (!authEntCodes.contains(req.getEntCode())) {
                    return null;
                }
            } else {
                req.setEntCodes(authEntCodes);
            }
        }
        return req;
    }

    @Override
    @Log(title = "应急通知", businessType = BusinessType.INSERT)
    public AjaxResult send(EmergencyNotice info) {
        info.setNoticeId(UlidCreator.getMonotonicUlid().toString());

        try {
            sendWeComMessage(info);
            info.setSendStatus("success");
        } catch (Exception e) {
            log.error("发送微信通知失败", e);
            info.setSendStatus("failed");
        }

        int rows = emergencyNoticeMapper.insert(info);
        return AjaxResult.success(rows);
    }

    @Override
    @Log(title = "应急通知", businessType = BusinessType.DELETE)
    public AjaxResult delete(EmergencyNotice info) {
        int rows = emergencyNoticeMapper.deleteById(info.getNoticeId());
        return AjaxResult.success(rows);
    }

    private void sendWeComMessage(EmergencyNotice info) {
        String content = buildMessageContent(info);
        // 发送给指定用户
        if (StringUtils.isNotEmpty(info.getReceiverNames())) {
            String toUser = info.getReceiverNames().replace(",", "|");
            weComSend.sendWXMessageToUserWithCard(toUser, info.getNoticeTitle(), content, "URL");
        }
        // 发送给企业关联的企业微信部门
        if (StringUtils.isNotEmpty(info.getEntCode())) {
            EnterpriseReq entReq = new EnterpriseReq();
            entReq.setEntCode(info.getEntCode());
            List<Enterprise> entList = enterpriseMapper.selectList(entReq);
            if (entList != null && !entList.isEmpty()) {
                Enterprise ent = entList.getFirst();
                if (StringUtils.isNotEmpty(ent.getWeComMsg())) {
                    weComSend.sendWXMessageToPartyWithCard(ent.getWeComMsg(), info.getNoticeTitle(), content, "URL");
                }
            }
        }
    }

    private String buildMessageContent(EmergencyNotice info) {
        StringBuilder sb = new StringBuilder();
        sb.append(info.getNoticeContent()).append("\n\n");
        if (StringUtils.isNotEmpty(info.getEventLocation())) {
            sb.append("地点：").append(info.getEventLocation()).append("\n");
        }
        if (info.getEventTime() != null) {
            sb.append("时间：").append(info.getEventTime().toString()).append("\n");
        }
        sb.append("请及时处理！");
        return sb.toString();
    }

    private void replaceReceiverNamesWithNickNames(List<EmergencyNotice> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<String> userNames = new LinkedHashSet<>();
        for (EmergencyNotice notice : list) {
            if (notice == null || StringUtils.isBlank(notice.getReceiverNames())) {
                continue;
            }
            String[] names = notice.getReceiverNames().split(",");
            for (String name : names) {
                String trimmedName = name == null ? null : name.trim();
                if (StringUtils.isNotBlank(trimmedName)) {
                    userNames.add(trimmedName);
                }
            }
        }
        if (userNames.isEmpty()) {
            return;
        }

        Set<String> queryUserNames = userNames.size() > FULL_NICKNAME_QUERY_THRESHOLD ? null : userNames;
        List<Map<String, String>> nickNameList = emergencyNoticeMapper.selectNickName(queryUserNames);
        if (nickNameList == null || nickNameList.isEmpty()) {
            for (EmergencyNotice notice : list) {
                if (notice != null) {
                    notice.setReceiverNames("");
                }
            }
            return;
        }
        Map<String, String> nickNameMap = nickNameList.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.get("userName")))
                .collect(Collectors.toMap(
                        item -> item.get("userName"),
                        item -> StringUtils.defaultIfBlank(item.get("nickName"), item.get("userName")),
                        (left, right) -> left
                ));

        for (EmergencyNotice notice : list) {
            if (notice == null || StringUtils.isBlank(notice.getReceiverNames())) {
                continue;
            }
            List<String> replacedNames = new ArrayList<>();
            String[] names = notice.getReceiverNames().split(",");
            for (String name : names) {
                String trimmedName = name == null ? null : name.trim();
                if (StringUtils.isBlank(trimmedName)) {
                    continue;
                }
                if (nickNameMap.containsKey(trimmedName)) {
                    replacedNames.add(nickNameMap.get(trimmedName));
                }
            }
            notice.setReceiverNames(String.join(",", replacedNames));
        }
    }
}

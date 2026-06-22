package com.zkhf.epmis.platform.send.weixin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.constant.CacheConstants;
import com.zkhf.epmis.core.redis.RedisCache;
import com.zkhf.epmis.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WeComSend {

    @Value("${data.send.wecom.corpId:}")
    private String corpId;

    @Value("${data.send.wecom.agentId:0}")
    private Integer agentId;

    @Value("${data.send.wecom.secretId:}")
    private String secretId;

    @Value("${data.send.wecom.expireTime:100}")
    private Integer expireTime;

    @Value("${data.send.wecom.getTokenUrl:https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s}")
    private String getTokenUrl;

    @Value("${data.send.wecom.sendMsgUrl:https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=}")
    private String sendMsgUrl;

    private RedisCache redisCache;

    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    public String getAccessToken() {
        return getAccessToken(corpId, agentId, secretId);
    }

    public void sendWXMessage(String toParty, String msg) {
        String token = getAccessToken();
        sendWXMessage(token, toParty, msg);
    }

    public void sendWXMessage(String token, String toParty, String msg) {
        if (StringUtils.isEmpty(token)) {
            log.error("未获取到的token corpId {}, agentId {}", corpId, agentId);
            return;
        }
        JSONObject sendData = new JSONObject();
        sendData.put("toparty", toParty);
        sendData.put("msgtype", "text");
        sendData.put("agentid", agentId);
        JSONObject content = new JSONObject();
        content.put("content", msg);
        sendData.put("text", content);
        
        sendPostRequest(sendMsgUrl + token, sendData.toJSONString());
    }

    public void sendWXMessageToPartyWithCard(String toParty, String title, String description, String url) {
        String token = getAccessToken();
        sendWXMessageToPartyWithCard(token, toParty, title, description, url);
    }

    public void sendWXMessageToPartyWithCard(String token, String toParty, String title, String description, String url) {
        if (StringUtils.isEmpty(token)) {
            log.error("未获取到的token corpId {}, agentId {}", corpId, agentId);
            return;
        }
        JSONObject sendData = new JSONObject();
        sendData.put("toparty", toParty);
        sendData.put("msgtype", "textcard");
        sendData.put("agentid", agentId);
        
        JSONObject textcard = new JSONObject();
        textcard.put("title", title);
        textcard.put("description", description);
        textcard.put("url", url != null ? url : "URL");
        textcard.put("btntxt", "详情");
        sendData.put("textcard", textcard);
        
        sendPostRequest(sendMsgUrl + token, sendData.toJSONString());
    }

    public void sendWXMessageToUserWithCard(String toUser, String title, String description, String url) {
        String token = getAccessToken();
        sendWXMessageToUserWithCard(token, toUser, title, description, url);
    }

    public void sendWXMessageToUserWithCard(String token, String toUser, String title, String description, String url) {
        if (StringUtils.isEmpty(token)) {
            log.error("未获取到的token corpId {}, agentId {}", corpId, agentId);
            return;
        }
        JSONObject sendData = new JSONObject();
        sendData.put("touser", toUser);
        sendData.put("msgtype", "textcard");
        sendData.put("agentid", agentId);
        
        JSONObject textcard = new JSONObject();
        textcard.put("title", title);
        textcard.put("description", description);
        textcard.put("url", url != null ? url : "URL");
        textcard.put("btntxt", "详情");
        sendData.put("textcard", textcard);
        
        sendPostRequest(sendMsgUrl + token, sendData.toJSONString());
    }

    private void sendPostRequest(String url, String body) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
            httpPost.setEntity(new StringEntity(body, "UTF-8"));

            HttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.debug("发送消息响应数据 {}", result);
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }

    private String getAccessToken(String corpId, Integer agentId, String secret) {
        if (StringUtils.isEmpty(corpId) || StringUtils.isEmpty(secret) || null == agentId || agentId <= 0) {
            return null;
        }
        String key = CacheConstants.WE_COM_ACCESS_TOKEN_KEY + corpId + ":" + agentId;
        String access_token = null;
        try {
            if (redisCache != null && redisCache.hasKey(key)) {
                return redisCache.getCacheObject(key);
            }
            String url = String.format(getTokenUrl, corpId, secret);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            String result = EntityUtils.toString(response.getEntity());
            JSONObject json = JSON.parseObject(result);
            if (json.getInteger("errcode") != null && json.getInteger("errcode") == 0) {
                access_token = json.getString("access_token");
            }
            if (redisCache != null) {
                if (null == access_token) {
                    // 未获取到token时先设置缓存3分钟
                    redisCache.setCacheObject(key, "", 3, TimeUnit.MINUTES);
                } else {
                    redisCache.setCacheObject(key, access_token, expireTime, TimeUnit.MINUTES);
                }
            }
        } catch (Exception e) {
            log.error("获取AccessToken失败", e);
        }
        return access_token;
    }
}

package com.zkhf.epmis.process.send.weixin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.constant.CacheConstants;
import com.zkhf.epmis.core.redis.RedisCache;
import com.zkhf.epmis.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
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

/**
 * AccessToken有效期为2小时，需要缓存并定时刷新
 * 消息内容长度限制：文本消息最长2048字节
 * 频率限制：每个应用不可超过1000次/分钟
 * 后期可更改为动态刷新touken，发消息时直接获取即可
 */
@Slf4j
@Component
public class WeComSend {
    /**
     * 企业微信id
     */
    @Value("${data.send.wecom.corpId}")
    private String corpId;
    /**
     * 企业微信自建应用agentId
     */
    @Value("${data.send.wecom.agentId}")
    private Integer agentId;
    /**
     * 企业微信自建应用secretid
     */
    @Value("${data.send.wecom.secretId}")
    private String secretId;
    /**
     * 企业微信的accessToken缓存失效时间(默认120分钟，这里设置100，防止token失效，单位分钟)
     */
    @Value("${data.send.wecom.expireTime:100}")
    private Integer expireTime;
    /**
     * 企业微信获取token的地址
     */
    @Value("${data.send.wecom.getTokenUrl:https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s}")
    private String getTokenUrl;
    /**
     * 企业微信发送消息的地址
     */
    @Value("${data.send.wecom.sendMsgUrl:https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=}")
    private String sendMsgUrl;

    private RedisCache redisCache;

    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    /**
     * 使用机器人发送消息，无需其他认证
     */
    public void sendWXMessageByRobot(String url, String msg) {
        JSONObject sendData = new JSONObject();
        sendData.put("msgtype", "text"); // 消息类型
        JSONObject content = new JSONObject();
        content.put("content", msg);
        sendData.put("text", content);
        log.debug("请求数据 {}", sendData);
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(sendData.toJSONString(), "UTF-8"));
            httpPost.setHeader("Content-Type", "application/json;charset=utf8");
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());
            // 处理响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("响应码：" + statusCode + " 发送结果: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAccessToken() {
        return getAccessToken(corpId, agentId, secretId);
    }

    /**
     * 企业微信部门发送消息
     */
    public void sendWXMessage(String toParty, String msg) {
        String token = getAccessToken(corpId, agentId, secretId);
        log.debug("获取到的token {}", token);
        sendWXMessage(token, toParty, msg);
    }

    /**
     * 企业微信部门发送消息
     */
    public void sendWXMessage(String token, String toParty, String msg) {
        if (StringUtils.isEmpty(token)) {
            log.error("未获取到的token corpId {}, agentId {}", corpId, agentId);
            return;
        }
        log.debug("发送企业微信信息 toParty {}, msg {}", toParty, msg);
        JSONObject sendData = getTextInfo(toParty, msg);
        sendPostRequest(sendMsgUrl + token, sendData.toJSONString());
    }

    private JSONObject getTextInfo(String toParty, String msg) {
        JSONObject sendData = new JSONObject();
        sendData.put("toparty", toParty); // 部门账号-发送到部门
        sendData.put("msgtype", "text"); // 消息类型
        sendData.put("agentid", agentId); // 企业应用的agentID
        sendData.put("content", agentId); // 企业应用的agentID
        JSONObject content = new JSONObject();
        content.put("content", msg);
        sendData.put("text", content);
        return sendData;
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
        String key = CacheConstants.WE_COM_ACCESS_TOKEN_KEY + corpId + ":" + agentId;
        String access_token = null;
        try {
            if (redisCache.hasKey(key)) {
                return redisCache.getCacheObject(key);
            }
            String url = String.format(getTokenUrl, corpId, secret);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            String result = EntityUtils.toString(response.getEntity());
            JSONObject json = JSON.parseObject(result);
            if (json.getInteger("errcode") == 0) {
                access_token = json.getString("access_token");
            }
            if (null == access_token) {
                // 未获取到token时先设置缓存3分钟
                redisCache.setCacheObject(key, "", 3, TimeUnit.MINUTES);
            } else {
                redisCache.setCacheObject(key, access_token, expireTime, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("获取AccessToken失败", e);
        }
        return access_token;
    }
}
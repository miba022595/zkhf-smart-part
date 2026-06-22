package com.zkhf.epmis.platform.base.controller;

import cn.hutool.core.util.XmlUtil;
import com.zkhf.epmis.platform.utils.util.WXBizMsgCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * @Description: 企业微信发送消息控制类
 * @Author: yanakai@126.com
 * @CreateDate: 2024-12-21 14:06
 * @UpdateUser: yanakai@126.com
 * @UpdateDate: 2024-12-21 14:06
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@RestController
@RequestMapping("/platform/freeApi/workWeChat")
public class WorkWeChatController {
    //内容填写自己企业微信中配置的
    //自建应用中的开通api接口消息详情里的sToken
    @Value("${freeApi.workWeChat.sToken:nq2hGi31at}")
    private String sToken;
    //企业微信后台的企业微信id
    @Value("${freeApi.workWeChat.sCorpID:ww1762f93cf724ed5d}")
    private String sCorpID;
    //自建应用的开通api接口消息详情里的sEncodingAESKey
    @Value("${freeApi.workWeChat.sEncodingAESKey:2tqOuklybabXSnTyebq9JQoyYqg1fGMHrL4berNNE10}")
    private String sEncodingAESKey;

    private Logger logger = Logger.getLogger(WorkWeChatController.class.getName());


    @RequestMapping("/getAccessToken")
    @ResponseBody
    private String test(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("回调开始");
        String method = request.getMethod();
        logger.info(method);
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
        // 微信加密签名
        String msg_signature = request.getParameter("msg_signature");
        logger.info("微信加密签名：" + msg_signature);
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 随机字符串
        String echostr = request.getParameter("echostr");
        String result = null;
        if (method.equals("GET")) {
            try {
                PrintWriter out = response.getWriter();
//                解密消息并将明文返回给企业微信，即可以开通api接收消息
                result = wxcpt.VerifyURL(msg_signature, timestamp, nonce, echostr);
                if (result == null) {
                    result = sToken;
                }
                out.print(result);
                out.close();
                return result;
            } catch (Exception e) {
                logger.info("验证URL失败，错误原因请查看异常:" + e);
                //验证URL失败，错误原因请查看异常
                e.printStackTrace();
            }

        } else {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");
            String timeMillis = System.currentTimeMillis() + "";
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(request.getInputStream());
            Element root = document.getDocumentElement();
            String corpID = root.getElementsByTagName("ToUserName").item(0).getTextContent();
            String agentID = root.getElementsByTagName("AgentID").item(0).getTextContent();
            String encrypt = root.getElementsByTagName("Encrypt").item(0).getTextContent();//用户发送的内容
            logger.info("CorpID：" + corpID);
            logger.info("agentID：" + agentID);
            logger.info("encrypt：" + encrypt);
            System.out.println("CorpID = " + corpID);
            System.out.println("agentID = " + agentID);
            System.out.println("encrypt = " + encrypt);
            result = wxcpt.VerifyURL(msg_signature, timestamp, nonce, encrypt);

            System.out.println("result = " + result);
            Document parse = XmlUtil.readXML(result);
            String fromUserName = parse.getElementsByTagName("FromUserName").item(0).getTextContent();
            String createTime = parse.getElementsByTagName("CreateTime").item(0).getTextContent();
            String msgType = parse.getElementsByTagName("MsgType").item(0).getTextContent();
            String content = parse.getElementsByTagName("Content").item(0).getTextContent();
            String msgId = parse.getElementsByTagName("MsgId").item(0).getTextContent();
            logger.info("FromUserName：" + fromUserName);
            System.out.println("FromUserName = " + fromUserName);
            System.out.println("CreateTime = " + createTime);
            System.out.println("MsgType = " + msgType);
            System.out.println("Content = " + content);
            System.out.println("MsgId = " + msgId);
            logger.info("MsgId：" + msgId);
            String msg = "";
            //对用户发送过来的内容选择要回复的内容
            //PS:这边你就可以对用户传过来的参数进行自己的判断，并增加自己的相关业务逻辑，返回回复的内容。
            if (content.contains("你好")) {
                content = content.substring(2);
                msg = "收到消息!";
            }
            String replyMsg = "<xml>"
                    + "<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>"//成员UserID
                    + "<FromUserName><![CDATA[" + corpID + "]]></FromUserName>"//企业微信CorpID
                    + "<CreateTime>" + timeMillis + "</CreateTime>"//消息创建时间（整型）
                    + "<MsgType><![CDATA[text]]></MsgType>"//消息类型，此时固定为：text
                    + "<Content><![CDATA[" + msg + "]]></Content>"//文本消息内容,最长不超过2048个字节，超过将截断
                    + "<MsgId>" + msgId + "</MsgId>"   //一定加
                    + "<AgentID>" + agentID + "</AgentID>"//一定加
                    + " </xml>";
            System.out.println("replyMsg = " + replyMsg);//打印出来
            logger.info("replyMsg：" + replyMsg);
            //这里可以存放你要自动回复给用户的信息
            String encryptMsg = wxcpt.EncryptMsg(replyMsg, timeMillis, nonce);
            System.out.println("11111111111encryptMsg = " + encryptMsg);
            logger.info("encryptMsg：" + encryptMsg);
            return encryptMsg;
        }
        return null;
    }


}

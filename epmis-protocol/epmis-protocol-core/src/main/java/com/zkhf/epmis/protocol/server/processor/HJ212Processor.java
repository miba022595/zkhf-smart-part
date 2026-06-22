package com.zkhf.epmis.protocol.server.processor;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.protocol.common.enums.DATAEnum;
import com.zkhf.epmis.protocol.common.utils.CRC16Util;
import com.zkhf.epmis.protocol.common.utils.DateUtil;
import com.zkhf.epmis.protocol.common.utils.StringUtil;
import com.zkhf.epmis.protocol.server.context.ChannelHolder;
import com.zkhf.epmis.protocol.server.mqtt.MqttClient;
import com.zkhf.epmis.protocol.util.PollCacheUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 解析HJ212协议
 */
@Slf4j
@Component
public class HJ212Processor {

    @Value("${mqtt.topics.out-put-data-real:out_put_data_real}")
    private String topicOutPutDataReal;

    @Value("${mqtt.topics.out-put-data-minute:out_put_data_minute}")
    private String topicOutPutDataMinute;

    @Value("${mqtt.topics.out-put-data-hour:out_put_data_hour}")
    private String topicOutPutDataHour;

    @Value("${mqtt.topics.out-put-data-day:out_put_data_day}")
    private String topicOutPutDataDay;

    @Value("${mqtt.topics.out-put-data-prefix:out_put_data_}")
    private String topicOutPutDataPrefix;

    private MqttClient mqttClient = null;
    @Autowired
    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void parse(ChannelHandlerContext ctx, String msg) {

        String exeRtn = "1";

        String mnStr = msg.substring(msg.indexOf("MN"));
        String mnNum = mnStr.substring(mnStr.indexOf("=") + 1, mnStr.indexOf(";"));

        InetSocketAddress inetSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = inetSocket.getAddress().getHostAddress();
        // 打印信息到日志文件
        log.debug("报文信息：mnNum {}，ip {}", mnNum, ip);

        // session 管理, 主要是为了对设备主动下发指令
        ChannelHolder.putChannelHandlerContext(ctx, mnNum);

        String[] body;
        if (msg.contains("QN")) {
            body = msg.substring(msg.indexOf("QN")).split("&&");
        } else {
            body = msg.substring(6).split("&&");
        }
        String head = body[0];
        String monitorData = body[1];
        String crc = body[2].replace("\r\n", "");

        String[] heads = head.split(";");
        JSONObject headMap = new JSONObject();
        headMap.put("from", "212");
        for (String string : heads) {
            if (string.split("=").length > 1) {
                headMap.put(string.split("=")[0], string.split("=")[1]);
            }
        }

        if (StringUtils.isBlank(monitorData)) {
            exeRtn = "100";
        }
        String[] dataList = monitorData.split(";");
        JSONObject dataMap = new JSONObject();
        for (String string : dataList) {
            String[] factors = string.split(",");
            for (String factor : factors) {
                if (string.split("=").length > 1) {
                    dataMap.put(factor.split("=")[0], factor.split("=")[1]);
                }
            }
        }

        String cn = headMap.getString("CN");
        String dataTime = dataMap.getString("DataTime");

        // 数据监测
        if (sourceDataCheck(ctx, headMap, msg, exeRtn, mnNum, cn, crc, dataTime, dataMap.getString("PolId"))) {
            return;
        }

        String dataType = DATAEnum.getEnumByCode(cn);

        log.debug("解析数据: dataType {}, headMap {}, dataMap {}", dataType, headMap, dataMap);

        // 发送解析数据到mqtt
        sendData(dataMap, mnNum, dataType);
    }

    private boolean sourceDataCheck(ChannelHandlerContext ctx, JSONObject headMap, String msg, String exeRtn, String mnNum, String cn, String crc, String dataTime, String polId) {
        String error = "";
        String qnRtn = "1";

        String qn = headMap.getString("QN");
        String st = headMap.getString("ST");
        String pw = headMap.getString("PW");
        String mn = headMap.getString("MN");
        String flag = headMap.getString("Flag");

        if (StringUtils.isBlank(qn)) {
            qnRtn = "7";
            exeRtn = "3";
            error += "缺少QN";
        }
        if (StringUtils.isBlank(st)) {
            qnRtn = "5";
            exeRtn = "3";
            error += "缺少ST";
        }
        if (StringUtils.isBlank(cn)) {
            qnRtn = "8";
            exeRtn = "3";
            error += "缺少CN";
        }
        if (StringUtils.isBlank(pw)) {
            qnRtn = "3";
            exeRtn = "3";
            error += "缺少PW";
        }
        if (StringUtils.isBlank(mn)) {
            qnRtn = "4";
            exeRtn = "3";
            error += "缺少MN";
        }
        if (!mnNum.equals(mn)) {
            qnRtn = "4";
            exeRtn = "3";
            error += "缺少MN";
        }
        if (StringUtils.isBlank(flag)) {
            qnRtn = "6";
            exeRtn = "3";
            error += "缺少FLAG";
        }
        if (StringUtils.isBlank(dataTime)) {
            error += "缺少DataTime";
        }

        // 校时
        if (cn.equals("9011") || cn.equals("9012")) {
//            throw new Exception("校时");
            return true;
        }
        if (cn.equals("1013")) {
            String ret = "QN=" + qn + ";ST=91;CN=1012;PW=" + pw + ";MN=" + mn + ";Flag=4;CP=&&&&";
            ret = "##" + StringUtils.length(ret) + ret + CRC16Util.calcCrc16(ret) + "\r\n";
            ByteBuf reply = Unpooled.buffer(ret.length());
            reply.writeBytes(ret.getBytes());
            ctx.writeAndFlush(reply);

            ret = "QN=" + qn + ";ST=91;CN=1012;PW=" + pw + ";MN=" + mn + ";Flag=4;CP=&&" + ((StringUtils.isNotBlank(dataTime)) ? ("PolId=" + polId + ";") : "") + "SystemTime=" + DateUtil.getCurrentDateStr(DateUtil.YY_M_D_H_S) + "&&";
            ret = "##" + StringUtils.length(ret) + ret + CRC16Util.calcCrc16(ret) + "\r\n";
            reply = Unpooled.buffer(ret.length());
            reply.writeBytes(ret.getBytes());
            ctx.writeAndFlush(reply);
//            throw new Exception("校时");
            return true;
        }

        if (flag != null) {
            StringBuilder buf = new StringBuilder();
            for (int i = 7; i >= 0; --i)
                buf.append(((1 << i) & (byte) Integer.parseInt(flag)) == 0 ? '0' : '1');
            String flagStr = buf.toString();

            // 拆包
            if (flagStr.charAt(6) == '1') {
//                String pNum = headMap.getString("PNUM");
//                String pNo = headMap.getString("PNO");
            }

            // 需要回复
            if (flagStr.charAt(7) == '1') {
                String ret = "QN=" + qn + ";ST=91;CN=9014;PW=" + pw + ";MN=" + mn + ";Flag=4;CP=&&QnRtn=" + qnRtn + ";ExeRtn=" + exeRtn + "&&";
                ret = "##" + StringUtil.getLength(ret) + ret + CRC16Util.calcCrc16(ret) + "\r\n";
                ByteBuf reply = Unpooled.buffer(ret.length());
                reply.writeBytes(ret.getBytes());
                ctx.writeAndFlush(reply);
            }
        }

        if (!error.isEmpty()) {
            log.debug(error);
            // 缺少报文属性继续解析
            // throw new Exception(error);
        }
        return false;
    }

    private void sendData(JSONObject dataMap, String mnNum, String dataType){

        JSONObject send = new JSONObject();

        // 格式化时间
        String dataTime = dataMap.getString("DataTime");

        Set<String> factors = new HashSet<>();
        for (String factor : dataMap.keySet()) {
            if (factor.contains("-")) {
                factors.add(factor.split("-")[0]);
            }
        }
        // 定义最终入库的因子值列表
        Map<String, Map<String, Object>> monitorData = new HashMap<>();
        Object val;
        for (String factor : factors) {
            Map<String, Object> map = new HashMap<>();
            val = dataMap.get(factor + "-Flag");
            map.put("flag", val);// N,F,M,S,D,C,T,B
            if (DATAEnum.REAL.desc.equals(dataType)) {
                val = dataMap.get(factor + "-Rtd");
                map.put("rtd", null == val ? 0 : val);
            } else {
                val = dataMap.get(factor + "-Avg");
                map.put("avg", null == val ? 0 : val);
                val = dataMap.get(factor + "-Max");
                map.put("max", val);
                val = dataMap.get(factor + "-Min");
                map.put("min", val);
            }
            val = dataMap.get(factor + "-Cou");
            if (null != val) {
                map.put("cou", val);
            }
            val = dataMap.get(factor + "-ZsRtd");
            if (null != val) {
                map.put("zsRtd", val);
            }
            val = dataMap.get(factor + "-ZsMin");
            if (null != val) {
                map.put("zsMin", val);
            }
            val = dataMap.get(factor + "-ZsAvg");
            if (null != val) {
                map.put("zsAvg", val);
            }
            val = dataMap.get(factor + "-ZsMax");
            if (null != val) {
                map.put("zsMax", val);
            }
            val = dataMap.get(factor + "-EFlag");
            if (null != val) { // 在线监控（检测）仪器仪表设备自行定义
                map.put("eFlag", val);
            }
            val = dataMap.get(factor + "-SampleTime");
            if (null != val) { // 采样时间
                map.put("sTime", val);
            }
            // 2017 编码替换 2005 编码，统一编码
            String lowCase = factor.toLowerCase();
            if (PollCacheUtils.pollCodeMap.containsKey(lowCase)) {
                // 防止报文信息有特殊因子，在基础因子编码表无法匹配到，而生成空因子数据问题
                monitorData.put(PollCacheUtils.pollCodeMap.get(lowCase), map);
            }
        }
        // 报文数据监测时间
        send.put("monitorTime", convertDateFormat(dataTime));
        // 设备的mn号
        send.put("mnNum", mnNum);
        // 报文数据类型 实时、分钟、小时、日
        send.put("dataType", dataType);
        // 报文监测数据
        send.put("monitorData", monitorData);
        try {
            log.debug("数据转发: {}", send.toJSONString());
            mqttClient.sendMessage(resolveOutPutDataTopic(dataType), send.toJSONString());
        } catch (Exception e) {
            log.error("数据转发失败: topic {}", dataType, e);
        }
    }

    private String resolveOutPutDataTopic(String dataType) {
        if (dataType == null) {
            return topicOutPutDataPrefix;
        }
        String type = dataType.trim().toLowerCase();
        return switch (type) {
            case "real" -> topicOutPutDataReal;
            case "minute" -> topicOutPutDataMinute;
            case "hour" -> topicOutPutDataHour;
            case "day" -> topicOutPutDataDay;
            default -> topicOutPutDataPrefix + dataType;
        };
    }

    private String convertDateFormat(String input) {
        if (input == null || input.length() != 14) {
            return null;
        }
        try {
            // 使用字符串截取和拼接
            return input.substring(0, 4) + "-" +  // yyyy
                    input.substring(4, 6) + "-" +  // MM
                    input.substring(6, 8) + " " +  // dd
                    input.substring(8, 10) + ":" + // HH
                    input.substring(10, 12) + ":" + // mm
                    input.substring(12, 14);        // ss
        } catch (Exception e) {
            return null;
        }
    }
}

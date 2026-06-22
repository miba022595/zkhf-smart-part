package com.zkhf.epmis.protocol.controller;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.protocol.server.context.ChannelHolder;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 指令下发控制类
 */
@Slf4j
@RestController
@RequestMapping("/protocol/cmd")
public class CmdSendController {

    /**
     * 指令下发
     */
    @PostMapping("/send")
    public JSONObject list(@RequestBody JSONObject test) {
        String mnNum = test.getString("mnNum");
        ChannelHandlerContext context = ChannelHolder.getChannelHandlerContext(mnNum);
        if (null != context) {
            log.info("发送消息到终端 {}", test);
//            context.writeAndFlush("");
        } else {
            log.info("终端未链接 {}", test);
        }
        return test;
    }

    /**
     * 在线设备信息查询
     */
    @GetMapping("/onlineList")
    public JSONObject onlineList() {
        JSONObject data = new JSONObject();
        data.put("chanelInfo", ChannelHolder.getAllChanelInfo());
        return data;
    }
}

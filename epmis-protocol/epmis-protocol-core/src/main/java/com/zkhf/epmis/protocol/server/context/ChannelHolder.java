package com.zkhf.epmis.protocol.server.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChannelHolder {

    private static final ConcurrentHashMap<String, ChannelHandlerContext> SESSION_MAP = new ConcurrentHashMap<>();

    private static final AttributeKey<String> SESSION_KEY = AttributeKey.valueOf("MN");

    public static void putChannelHandlerContext(ChannelHandlerContext ctx, String mn) {
        ctx.channel().attr(SESSION_KEY).set(mn);
        SESSION_MAP.put(mn, ctx);
    }

    public static void removeChannelHandlerContext(ChannelHandlerContext ctx) {
        String mn = ctx.channel().attr(SESSION_KEY).get();
        if (StringUtils.isBlank(mn)) {
            log.info("Channel {} 没有绑定MN", ctx.channel().id().toString());
            return;
        }
        SESSION_MAP.remove(mn);
    }

    public static String getMN(ChannelHandlerContext ctx) {
        return ctx.channel().attr(SESSION_KEY).get();
    }

    public static ChannelHandlerContext getChannelHandlerContext(String mn) {
        return SESSION_MAP.get(mn);
    }

    public static Map<String, String> getAllChanelInfo() {
        Map<String, String> map = new ConcurrentHashMap<>();
        for (Map.Entry<String, ChannelHandlerContext> entry : SESSION_MAP.entrySet()) {
            map.put(entry.getKey(), entry.getValue().channel().id().toString());
        }
        return map;
    }
}

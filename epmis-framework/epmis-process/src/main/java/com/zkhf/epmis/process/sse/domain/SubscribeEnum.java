package com.zkhf.epmis.process.sse.domain;

/**
 * 订阅的消息类型
 */
public enum SubscribeEnum {
    /** 连接消息 */
    connected,
    /** 失败 */
    success,
    /** 失败 */
    fail,
    /** 在线监测数据报警订阅 */
    onlineDataAlarm,
}
package com.zkhf.epmis.protocol.modbus.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务端作为从机，设备为主机
 * 写就是设备把信息写到流，让服务端接收
 * 读就是服务端下发指令到设备
 */
public enum FunctionCode {
    // 位操作
//    READ_COILS(0x01, "读线圈"),
//    READ_DISCRETE_INPUTS(0x02, "读取离散输入"),
    WRITE_SINGLE_COIL(0x05, "写入单线圈"),
    WRITE_MULTIPLE_COILS(0x0F, "写入多个线圈"),

    // 字操作
//    READ_HOLDING_REGISTERS(0x03, "读取保持寄存器"),
//    READ_INPUT_REGISTERS(0x04, "读取输入型寄存器"),
    WRITE_SINGLE_REGISTER(0x06, "写入单个寄存器"),
    WRITE_MULTIPLE_REGISTERS(0x10, "写入多个寄存器"),

    // 文件记录
//    READ_FILE_RECORD(0x14, "读文件记录"),
//    WRITE_FILE_RECORD(0x15, "写文件记录"),
//    READ_WRITE_MULTIPLE_REGISTERS(0x17, "读写多个寄存器"),

    // 诊断
//    DIAGNOSTICS(0x08, "诊断"),
//    GET_COMM_EVENT_COUNTER(0x0B, "获取通信事件计数器"),
//    GET_COMM_EVENT_LOG(0x0C, "获取通信事件日志"),
    REPORT_SLAVE_ID(0x11, "报告从机ID"),
    ENCAPSULATED_INTERFACE_TRANSPORT(0x2B, "封装接口传输"),

    // 其他
//    READ_EXCEPTION_STATUS(0x07, "读异常状态"),
//    MASK_WRITE_REGISTER(0x16, "屏蔽写寄存器"),
//    READ_FIFO_QUEUE(0x18, "读FIFO队列");
    ;

    public final Integer code;
    public final String description;

    FunctionCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<Integer, String> codeMap =
            Arrays.stream(FunctionCode.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.code,
                            e -> e.description
                    ));

    public static boolean containsCode(Integer code) {
        if (null == code) {
            return false;
        }
        return codeMap.containsKey(code);
    }
}

package com.zkhf.epmis.process.mqtt.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 实时数据缓存数据结构
 */
@Data
public class RealCacheData {

    /**
     * 检测时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /**
     * 检测的数据
     */
    private Map<String, Object> data;
}

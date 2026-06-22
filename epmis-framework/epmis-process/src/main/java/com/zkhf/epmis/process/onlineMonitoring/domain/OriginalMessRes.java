package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OriginalMessRes {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime dataTime;

    private String message;

    public OriginalMessRes(LocalDateTime dataTime, String message) {
        this.dataTime = dataTime;
        this.message = message;
    }
}

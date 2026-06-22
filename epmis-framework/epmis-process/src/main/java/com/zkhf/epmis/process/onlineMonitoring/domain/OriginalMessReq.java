package com.zkhf.epmis.process.onlineMonitoring.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OriginalMessReq {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private String st;
    private String cn;
    private String mn;
    private String outPutId;
    private List<File> fileList;
}

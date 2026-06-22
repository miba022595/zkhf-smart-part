package com.zkhf.epmis.process.mqtt.domain;

import lombok.Data;

@Data
public class ParseResult {
    private String messageId;
    private String payload;
    private String parentDir;
    private boolean isBadFile;
}

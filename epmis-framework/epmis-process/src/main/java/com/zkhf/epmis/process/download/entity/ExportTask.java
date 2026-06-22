package com.zkhf.epmis.process.download.entity;

import lombok.Data;

import java.io.File;

@Data
public class ExportTask {
    private final String taskId;
    private volatile ExportStatus status;
    private File resultFile;
    private String errorMessage;
    private int processedCount;

    public ExportTask(String taskId) {
        this.taskId = taskId;
        this.status = ExportStatus.PENDING;
    }

    // getters and setters
}
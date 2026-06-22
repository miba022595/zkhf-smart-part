package com.zkhf.epmis.process.download.service;

import com.zkhf.epmis.process.download.entity.ExportStatus;

import java.io.File;

public interface ExportService {

    String startExportTask();

    // 其他辅助方法...
    ExportStatus getExportStatus(String taskId);

    File getExportFile(String taskId);
}

package com.zkhf.epmis.process.download.controller;

import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.process.download.entity.ExportStatus;
import com.zkhf.epmis.process.download.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// 可用通过sse发送请求，下载文件，返回数据中列出任务id，执行速率，执行完成后，再通过接口获取文件，或者直接返回映射地址，下载文件
@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/excel")
    public ResponseEntity<String> triggerExport() {
        String taskId = exportService.startExportTask();
        return ResponseEntity.ok()
                .body("{\"taskId\": \"" + taskId + "\"}");
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<String> checkExportStatus(@PathVariable String taskId) {
        ExportStatus status = exportService.getExportStatus(taskId);
        return ResponseEntity.ok()
                .body("{\"status\": \"" + status.name() + "\"}");
    }

    @GetMapping("/download/{taskId}")
    public void downloadExportFile(@PathVariable String taskId,
                                   HttpServletResponse response) throws IOException {
        File file = exportService.getExportFile(taskId);
        if (file == null || !file.exists()) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Export file not found");
            return;
        }

        response.setContentType(MimeTypeUtils.EXCEL_XLSX);
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
        response.setContentLength((int) file.length());

        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            FileCopyUtils.copy(in, out);
        }
    }
}

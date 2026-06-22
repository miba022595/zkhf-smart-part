package com.zkhf.epmis.process.download.service.impl;

import com.zkhf.epmis.process.download.entity.ExportStatus;
import com.zkhf.epmis.process.download.entity.ExportTask;
import com.zkhf.epmis.process.download.service.ExportService;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExportServiceImpl implements ExportService {

    private final Map<String, ExportTask> tasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    @Override
    public String startExportTask() {
        String taskId = UUID.randomUUID().toString();
        ExportTask task = new ExportTask(taskId);
        tasks.put(taskId, task);

        executor.execute(() -> {
            try {
                task.setStatus(ExportStatus.PROCESSING);
                exportToExcel(task);
                task.setStatus(ExportStatus.COMPLETED);
            } catch (Exception e) {
                task.setStatus(ExportStatus.FAILED);
                task.setErrorMessage(e.getMessage());
            }
        });

        return taskId;
    }

    private void exportToExcel(ExportTask task) throws IOException {
        String tempFileName = "export_" + task.getTaskId() + ".xlsx";
        File file = new File(System.getProperty("java.io.tmpdir"), tempFileName);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            SXSSFSheet sheet = workbook.createSheet("数据");

            // 创建标题行

            // 分页查询数据
            int page = 0;
            int pageSize = 1000;
            int rowNum = 1;

            while (true) {
                List<String> dataList = new ArrayList<>();
                if (dataList.isEmpty()) {
                    break;
                }

                for (String data : dataList) {
//                    SXSSFRow row = sheet.createRow(rowNum++);
//                    createDataRow(row, data);

                    // 更新进度 todo 可以异步推送到前端
                    task.setProcessedCount(rowNum - 1);
                }

                // 定期刷新到磁盘
                if (rowNum % 1000 == 0) {
                    sheet.flushRows(100);
                }

                page++;
            }

            // 写入临时文件
            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }

            task.setResultFile(file);
        }
    }

    @Override
    public ExportStatus getExportStatus(String taskId) {
        ExportTask task = tasks.get(taskId);
        return task != null ? task.getStatus() : ExportStatus.NOT_FOUND;
    }

    @Override
    public File getExportFile(String taskId) {
        ExportTask task = tasks.get(taskId);
        return task != null ? task.getResultFile() : null;
    }
}

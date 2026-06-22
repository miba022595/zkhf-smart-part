package com.zkhf.epmis.platform.envManual.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstance;
import com.zkhf.epmis.platform.approval.enums.ApprovalInstanceStatus;
import com.zkhf.epmis.platform.approval.service.ApprovalService;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlan;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTaskReq;
import com.zkhf.epmis.platform.envManual.domain.EnvManualInitTask;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import com.zkhf.epmis.platform.envManual.enums.PlanStatusType;
import com.zkhf.epmis.platform.envManual.enums.TaskStatusType;
import com.zkhf.epmis.platform.envManual.service.EnvManualCheckTaskService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envManual.EnvManualCheckTaskMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 环境手工检测任务Service业务层处理
 */
@Slf4j
@Service("envManualCheckTaskService")
public class EnvManualCheckTaskServiceImpl implements EnvManualCheckTaskService, ApprovalService {

    private EnvManualCheckTaskMapper envManualCheckTaskMapper;
    @Autowired
    public void setEnvManualCheckTaskMapper(EnvManualCheckTaskMapper envManualCheckTaskMapper) {
        this.envManualCheckTaskMapper = envManualCheckTaskMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public void initTask(Integer oldStatus, EnvManualCheckPlan info) {
        LocalDate now = LocalDate.now();
        // 首次执行时间不能超过早于当天
        if (null == info.getFirstDate() || info.getFirstDate().isBefore(now)) {
            return;
        }
        // 计划检测频次为空跳过
        if (StringUtils.isEmpty(info.getCheckFrequency())) {
            return;
        }
        List<EnvManualInitTask> taskList = new ArrayList<>();
        // 状态由待审批变更为已审批状态时，可生成任务
        if (PlanStatusType.STATUS_DSP.code.equals(oldStatus) &&
                PlanStatusType.STATUS_YSP.code.equals(info.getStatus())
        ) {
            // +1 保证isBefore能截至到一周内的最后一天
            LocalDate max = now.plusDays(6 + 1);
            LocalDate next;
            for (String freq : info.getCheckFrequency().split(",")) {
                int code = StringUtils.strToInt(freq, 0);
                next = info.getFirstDate();
                if (CheckFrequencyType.TYPE_R.code.equals(code)) { // 日次
                    while (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_R.code);
                        next = next.plusDays(1);
                    }
                } else if (CheckFrequencyType.TYPE_Z.code.equals(code)) { // 周次（时间在范围内只会命中一次）
                    if (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_Z.code);
                    }
                }  else if (CheckFrequencyType.TYPE_Y.code.equals(code)) { // 月次（时间在范围内只会命中一次）
                    if (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_Y.code);
                    }
                } else if (CheckFrequencyType.TYPE_JD.code.equals(code)) { // 季度（时间在范围内只会命中一次）
                    if (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_JD.code);
                    }
                } else if (CheckFrequencyType.TYPE_BN.code.equals(code)) { // 半年（时间在范围内只会命中一次）
                    if (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_BN.code);
                    }
                } else if (CheckFrequencyType.TYPE_N.code.equals(code)) { // 年（时间在范围内只会命中一次）
                    if (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_N.code);
                    }
                } else if (CheckFrequencyType.TYPE_LN.code.equals(code)) { // 两年一次（时间在范围内只会命中一次）
                    if (next.isBefore(max)) {
                        addTask(taskList, info.getOutPutPollId(), next, CheckFrequencyType.TYPE_LN.code);
                    }
                }
            }
        }
        if (!taskList.isEmpty()) {
            envManualCheckTaskMapper.batchInsertEnvManualCheckTask(taskList);
        }
    }

    private void addTask(List<EnvManualInitTask> taskList, String outPutPollId, LocalDate taskDate, Integer checkFrequency) {
        taskList.add(EnvManualInitTask.builder()
                        .taskId(UlidCreator.getMonotonicUlid().toString())
                        .outPutPollId(outPutPollId)
                        .taskDate(taskDate)
                        .status(TaskStatusType.STATUS_DXF.code)
                        .checkFrequency(checkFrequency)
                .build());
    }

    @Override
    public AjaxResult selectEnvManualCheckTaskList(EnvManualCheckTaskReq req) {
        // 请求参数转换
        req = initReq(req);
        // 分页查询
        boolean page = PageUtils.startPageCheckExists();
        List<EnvManualCheckTask> list = envManualCheckTaskMapper.selectEnvManualCheckTaskList(req);
        // 数据填充
        fill(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "环境手工检测任务", businessType = BusinessType.EXPORT)
    public void exportEnvManualCheckTask(EnvManualCheckTaskReq req, HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环境手工检测任务模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 请求参数转换
            req = initReq(req);
            List<EnvManualCheckTask> list = envManualCheckTaskMapper.selectEnvManualCheckTaskList(req);
            if (null != list && !list.isEmpty()) {
                // 数据填充
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 从第（rowIndex + 1）行开始插入

                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                int index = 1;
                Row row;
                Cell cell;
                List<CellRangeAddress> mergeRegions = new ArrayList<>();

                int groupStartRow = rowIndex;
                int groupSize = 0;
                String prevOutPutId = null;
                String prevOutPutPollId = null;
                // 添加污染物组的起始行记录
                int pollutantGroupStartRow = rowIndex;
                int pollutantGroupSize = 0;

                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (int i = 0; i < list.size(); i++) {
                    EnvManualCheckTask info = list.get(i);

                    row = sheet.createRow(rowIndex);

                    int cellIndex = 0;

                    // 判断是否是排放口组的开始
                    boolean isOutPutGroupStart = !info.getOutPutId().equals(prevOutPutId);
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isOutPutGroupStart) {
                        cell.setCellValue(index);
                    }
                    // 排放口名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isOutPutGroupStart) {
                        cell.setCellValue(info.getOutPutName());
                    }
                    // 判断是否是污染物组的开始
                    boolean isPollutantGroupStart = isOutPutGroupStart || !info.getOutPutPollId().equals(prevOutPutPollId);
                    // 排放口类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isOutPutGroupStart) {
                        cell.setCellValue(OutPutTypeEnum.getNameByCode(info.getOutPutType()));
                    }
                    // 排口关联污染物code
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isPollutantGroupStart) {
                        cell.setCellValue(info.getPollutantCode());
                    }
                    // 排口关联污染物名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isPollutantGroupStart) {
                        cell.setCellValue(info.getPollutantNameCn());
                    }
                    // 其他列每行都显示
                    // 任务日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getTaskDate()) {
                        cell.setCellValue(info.getTaskDate().format(DateUtils.yy_m_d));
                    }
                    // 任务状态
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getStatusDesc());
                    // 检测单位
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMonitorUnitName());
                    // 任务描述
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getTaskDesc());
                    // 报告名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getReportName());
                    // 报告编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getReportCode());
                    // 监测类别
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMonitorCategory());
                    // 采样日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getSampleDate()) {
                        cell.setCellValue(info.getSampleDate().format(DateUtils.yy_m_d));
                    }
                    // 报告日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getReportDate()) {
                        cell.setCellValue(info.getReportDate().format(DateUtils.yy_m_d));
                    }
                    // 分析/监测周期-开始时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getAnalysisStartDate()) {
                        cell.setCellValue(info.getAnalysisStartDate().format(DateUtils.yy_m_d));
                    }
                    // 分析/监测周期-结束时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getAnalysisEndDate()) {
                        cell.setCellValue(info.getAnalysisEndDate().format(DateUtils.yy_m_d));
                    }
                    // 分析/监测人员
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getAnalysisPerson());
                    // 采样人员
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getSamplingPerson());
                    // 本期运行时间(h)
                    cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    if (null != info.getOperationHour()) {
                        cell.setCellValue(info.getOperationHour());
                    }
                    // 联系人
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getContactPerson());
                    // 联系电话
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getContactPhone());

                    rowIndex++;
                    groupSize++;
                    pollutantGroupSize++;

                    // 判断是否是排放口组的结束
                    boolean isOutPutGroupEnd = (i == list.size() - 1) ||
                            !info.getOutPutId().equals(list.get(i + 1).getOutPutId());
                    // 判断是否是污染物组的结束
                    boolean isPollutantGroupEnd = isOutPutGroupEnd ||
                            (i < list.size() - 1 && !info.getOutPutPollId().equals(list.get(i + 1).getOutPutPollId()));

                    // 处理污染物组合并
                    if (isPollutantGroupEnd) {
                        // 如果污染物组有多行，合并污染物列
                        if (pollutantGroupSize > 1) {
                            // 合并污染物code列（第4列，索引3）
                            mergeRegions.add(new CellRangeAddress(pollutantGroupStartRow, pollutantGroupStartRow + pollutantGroupSize - 1, 3, 3));
                            // 合并污染物名称列（第5列，索引4）
                            mergeRegions.add(new CellRangeAddress(pollutantGroupStartRow, pollutantGroupStartRow + pollutantGroupSize - 1, 4, 4));
                        }
                        // 重置污染物组信息
                        pollutantGroupStartRow = rowIndex;
                        pollutantGroupSize = 0;
                    }

                    // 处理排放口组合并
                    if (isOutPutGroupEnd) {
                        // 如果排放口组有多行，合并排放口相关列
                        if (groupSize > 1) {
                            // 合并序号列（第1列，索引0）
                            mergeRegions.add(new CellRangeAddress(groupStartRow, groupStartRow + groupSize - 1, 0, 0));
                            // 合并排放口名称列（第2列，索引1）
                            mergeRegions.add(new CellRangeAddress(groupStartRow, groupStartRow + groupSize - 1, 1, 1));
                            // 合并排放口类型列（第3列，索引2）
                            mergeRegions.add(new CellRangeAddress(groupStartRow, groupStartRow + groupSize - 1, 2, 2));
                        }
                        // 重置排放口组信息
                        groupStartRow = rowIndex;
                        groupSize = 0;
                        index++; // 序号递增
                    }

                    prevOutPutId = info.getOutPutId();
                    prevOutPutPollId = info.getOutPutPollId();
                }

                // 应用合并区域
                for (CellRangeAddress mergeRegion : mergeRegions) {
                    sheet.addMergedRegion(mergeRegion);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环境手工检测任务.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("outputStream close", e);
                }
            }
        }
    }

    private EnvManualCheckTaskReq initReq(EnvManualCheckTaskReq req) {
        if (null == req) {
            req = new EnvManualCheckTaskReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        if (StringUtils.isNotEmpty(req.getTaskDateStart()) && StringUtils.isNotEmpty(req.getTaskDateEnd())) {
            req.setTaskDateStart(req.getTaskDateStart() + " 00:00:00");
            req.setTaskDateEnd(req.getTaskDateEnd() + " 23:59:59");
        } else {
            req.setTaskDateStart(null);
            req.setTaskDateEnd(null);
        }
        return req;
    }

    private void fill(List<EnvManualCheckTask> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        list.forEach( e -> {
            e.setStatusDesc(TaskStatusType.getNameByCode(e.getStatus()));
            e.setCheckFrequencyDesc(CheckFrequencyType.getNameByCode(e.getCheckFrequency()));
        });
    }

    @Override
    @Log(title = "环境手工检测任务", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvManualCheckTask(EnvManualCheckTask info) {
        EnvManualCheckTask old = envManualCheckTaskMapper.selectEnvManualCheckByTaskId(info.getTaskId());
        if (null == old) {
            return AjaxResult.error("检测任务不存在");
        }
        String errMsg = null;
        if (TaskStatusType.STATUS_SPZ.code.equals(old.getStatus())) {
            errMsg = "任务审批中，不可修改";
        } else if (TaskStatusType.STATUS_YQX.code.equals(old.getStatus())) {
            errMsg = "任务已取消，不允许修改";
        } else if (TaskStatusType.STATUS_YWC.code.equals(old.getStatus())) {
            errMsg = "任务已完成，不允许修改";
        }
        if (null != errMsg) {
            return AjaxResult.error(errMsg);
        }
        if (TaskStatusType.STATUS_DXF.code.equals(old.getStatus()) && TaskStatusType.STATUS_YWC.code.equals(info.getStatus())) {
            return AjaxResult.error("状态不能越级变更 " + TaskStatusType.STATUS_DXF.name + " -》 " + TaskStatusType.STATUS_YWC.name);
        }
        if (TaskStatusType.STATUS_DZX.code.equals(old.getStatus()) && TaskStatusType.STATUS_DXF.code.equals(info.getStatus())) {
            return AjaxResult.error("状态不能越级变更 " + TaskStatusType.STATUS_DZX.name + " -》 " + TaskStatusType.STATUS_DXF.name);
        }
        int result = envManualCheckTaskMapper.updateEnvManualCheckTask(info);
        // 修改附件信息
        if (result > 0) {
            annexService.updateAnnex(info.getTaskId(), AnnexTypeEnum.envManualCheckTask.name(), info.getAnnexIds());
        }
        return AjaxResult.success(result);
    }

    @Override
    @Log(title = "环境手工检测任务报告", businessType = BusinessType.EXPORT)
    public AjaxResult downloadReportTemplate(EnvManualCheckTaskReq req, HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环境手工检测任务报告模板.xlsx");
            if (workbook == null) {
                return AjaxResult.error("未获取到模板文件");
            }
            Sheet sheet0 = workbook.getSheetAt(0);

            Row row = sheet0.getRow(0);
            Cell cell = row.getCell(1);
            cell.setCellValue("示例报告名称");
            cell = row.getCell(3);
            cell.setCellValue("v-1.1.1");
            cell = row.getCell(5);
            cell.setCellValue("示例监测类别");

            row = sheet0.getRow(1);
            cell = row.getCell(1);
            cell.setCellValue("xxx");
            cell = row.getCell(3);
            cell.setCellValue("1999-01-01");
            cell = row.getCell(5);
            cell.setCellValue("xxx");

            row = sheet0.getRow(2);
            cell = row.getCell(1);
            cell.setCellValue("1999-01-01");
            cell = row.getCell(3);
            cell.setCellValue("xxx");
            cell = row.getCell(5);
            cell.setCellValue("19354581234");

            row = sheet0.getRow(3);
            cell = row.getCell(1);
            cell.setCellValue("1999-01-01");
            cell = row.getCell(3);
            cell.setCellValue("1999-01-01");

            List<EnvManualCheckTask> list;
            if (null == req || null == req.getTaskIdList() || req.getTaskIdList().isEmpty()) {
                list = new ArrayList<>();
            } else {
                // 请求参数转换
                req = initReq(req);
                // 只能获取可上传报告的列表， 任务状态：0-已取消，1-待下发，2-待执行，3-已完成
                req.setStatusList(Arrays.asList(TaskStatusType.STATUS_DZX.code, TaskStatusType.STATUS_YWC.code));
                list = envManualCheckTaskMapper.selectEnvManualCheckTaskList(req);
            }
            if (null != list && !list.isEmpty()) {
                Sheet sheet1 = workbook.getSheetAt(1);
                // 数据填充
                fill(list);
                int rowIndex = 2;// 从第（rowIndex + 1）行开始插入

                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet1, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet1, rowIndex, 0,0);
                CellStyle styleN3 = CellUtils.getCellStyle(workbook, sheet1, rowIndex, 0,3);

                int cellIndex, index = 1;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet1, rowIndex, list.size());
                for (EnvManualCheckTask info : list) {
                    row = sheet1.createRow(rowIndex);

                    cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 排放口名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getOutPutName());
                    // 监测因子
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutantNameCn());
                    // 监测频次
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getCheckFrequencyDesc());
                    // 排放限值-最大值
                    cell = CellUtils.getCell(row, cellIndex++, styleN3);
                    if (null != info.getOverMaxvalue()) {
                        cell.setCellValue(info.getOverMaxvalue());
                    }
                    // 排放限值-最小值
                    cell = CellUtils.getCell(row, cellIndex++, styleN3);
                    if (null != info.getOverMinvalue()) {
                        cell.setCellValue(info.getOverMinvalue());
                    }
                    // 获取空单元格，样式设置
                    CellUtils.getCell(row, cellIndex++, styleN0);
                    CellUtils.getCell(row, cellIndex++, styleN3);
                    CellUtils.getCell(row, cellIndex++, styleN3);
                    CellUtils.getCell(row, cellIndex++, styleN3);
                    CellUtils.getCell(row, cellIndex  , styleN3);
                    rowIndex++;
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环境手工检测任务报告模板.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("outputStream close", e);
                }
            }
        }
        return null;
    }

    @Override
    public List<EnvManualCheckTask> selectEnvManualCheckTaskForReport(List<String> taskIdList) {
        if (null == taskIdList || taskIdList.isEmpty()) {
            return new ArrayList<>();
        }
        // 请求参数转换
        EnvManualCheckTaskReq req = new EnvManualCheckTaskReq();
        // 只能获取可上传报告的列表， 任务状态：0-已取消，1-待下发，2-待执行，3-已完成
        req.setStatusList(Arrays.asList(TaskStatusType.STATUS_DZX.code, TaskStatusType.STATUS_YWC.code));
        req.setTaskIdList(taskIdList);
        List<EnvManualCheckTask> list = envManualCheckTaskMapper.selectEnvManualCheckTaskList(req);
        // 数据填充
        fill(list);
        return list;
    }

    @Override
    @Log(title = "环境手工检测任务报告导入", businessType = BusinessType.UPDATE)
    public void batchUpdateEnvManualCheckTask(List<EnvManualCheckTask> taskList) {
        if (null == taskList || taskList.isEmpty()) {
            return;
        }
        // 状态变更为已完成
        taskList.forEach( e -> e.setStatus(TaskStatusType.STATUS_YWC.code));
        int result = envManualCheckTaskMapper.batchUpdateEnvManualCheckTask(taskList);
        // 修改附件信息
        if (result > 0) {
            taskList.forEach( e -> annexService.updateAnnex(e.getTaskId(), AnnexTypeEnum.envManualCheckTask.name(), e.getAnnexIds()));
        }
    }

    @Async
    @Override
    public void approval(ApprovalInstance instance) {
        if (instance == null) {
            return;
        }
        EnvManualCheckTask task = new EnvManualCheckTask();
        Integer newStatus;
        if (ApprovalInstanceStatus.PROCESSING.code.equals(instance.getStatus())) {
            newStatus = TaskStatusType.STATUS_SPZ.code;
        } else if (ApprovalInstanceStatus.APPROVED.code.equals(instance.getStatus())) {
            newStatus = TaskStatusType.STATUS_DZX.code;
        } else if (ApprovalInstanceStatus.REJECTED.code.equals(instance.getStatus())) {
            newStatus = TaskStatusType.STATUS_YJJ.code;
        } else if (ApprovalInstanceStatus.CANCELLED.code.equals(instance.getStatus())) {
            newStatus = TaskStatusType.STATUS_YQX.code;
        } else {
            return;
        }
        task.setTaskId(instance.getBusinessKey());
        task.setStatus(newStatus);
        envManualCheckTaskMapper.updateEnvManualCheckTask(task);
    }
}


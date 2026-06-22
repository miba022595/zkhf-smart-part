package com.zkhf.epmis.process.envManual.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckReport;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckReportDetail;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.process.envManual.service.EnvManualCheckService;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.mapper.envManual.EnvManualCheckMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

/**
 * 环境手工检测任务Service业务层处理
 */
@Slf4j
@Service
public class EnvManualCheckServiceImpl implements EnvManualCheckService {

    private EnvManualCheckMapper envManualCheckMapper;
    @Autowired
    public void setEnvManualCheckTaskMapper(EnvManualCheckMapper envManualCheckMapper) {
        this.envManualCheckMapper = envManualCheckMapper;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    @Override
    @Log(title = "环境手工检测任务报告", businessType = BusinessType.IMPORT)
    public AjaxResult importReportTemplate(List<String> taskIdList, MultipartFile file) {
        if (null == taskIdList || taskIdList.isEmpty()) {
            return AjaxResult.error("报告需指定任务列表");
        }
        EnvManualCheckReport report = new EnvManualCheckReport();
        String errInfo = parseReportExcel(file, report);
        if (null != errInfo) {
            return AjaxResult.error(errInfo);
        }
        if (null == report.getDetailList() || report.getDetailList().isEmpty()) {
            return AjaxResult.error("报告数据项列表为空");
        }
        // file需要转成附件存储
        AnnexInfo annexInfo = platformFacade.uploadAnnex(file, AnnexTypeEnum.envManualCheckTask.name);
        if (null != annexInfo) {
            report.setAnnexIds(Collections.singletonList(annexInfo.getAnnexId()));
        }
        // k outPutName -> pollutantCodeName -> checkFrequencyDesc  v detailId
        Map<String, Map<String, Map<String, EnvManualCheckReportDetail>>> detailMap = new HashMap<>();
        report.getDetailList().forEach( e -> {
            Map<String, Map<String, EnvManualCheckReportDetail>> outMap = detailMap.computeIfAbsent(e.getOutPutName(), k -> new HashMap<>());
            Map<String, EnvManualCheckReportDetail> pollMap = outMap.computeIfAbsent(e.getPollutantCodeName(), k -> new HashMap<>());
            pollMap.put(e.getCheckFrequencyDesc(), e);
        });
        List<EnvManualCheckTask> taskList = platformFacade.selectEnvManualCheckTaskForReport(taskIdList);
        List<EnvManualCheckReportDetail> detailList = new ArrayList<>();
        for (EnvManualCheckTask task : taskList) {
            Map<String, Map<String, EnvManualCheckReportDetail>> outMap = detailMap.get(task.getOutPutName());
            if (null == outMap) {
                return AjaxResult.error("报告中无 [" + task.getOutPutName() + "] 排口数据");
            }
            Map<String, EnvManualCheckReportDetail> pollMap = outMap.get(task.getPollutantNameCn());
            if (null == pollMap) {
                return AjaxResult.error("报告中无 [" + task.getOutPutName() + "] 排口的 [" + task.getPollutantNameCn() + "] 污染物数据");
            }
            EnvManualCheckReportDetail detail = pollMap.get(task.getCheckFrequencyDesc());
            if (null == detail) {
                return AjaxResult.error("报告中无 [" + task.getOutPutName() + "] 排口的 [" +
                        task.getPollutantNameCn() + "] 污染物的 [" +
                        task.getCheckFrequencyDesc() + "] 检测数据");
            }
            setData(task, detail, report);
            detailList.add(detail);
        }
        // 更新任务中的报告信息
        platformFacade.batchUpdateEnvManualCheckTask(taskList);
        // 报告数据入库
        int count = 0;
        if (!detailList.isEmpty()) {
            count = envManualCheckMapper.batchInsertEnvManualCheckData(detailList);
        }
        return AjaxResult.success(count);
    }

    private void setData(EnvManualCheckTask task, EnvManualCheckReportDetail detail, EnvManualCheckReport report) {
        detail.setOutPutId(task.getOutPutId());
        detail.setPollutantCode(task.getPollutantCode());
        detail.setCheckFrequency(task.getCheckFrequency());
        // 使用String.format确保3位数字，不足补零
        String formattedDataType = String.format("%03d", task.getCheckFrequency());
        detail.setOutId((null == report.getSampleDate() ? "--------" : report.getSampleDate().format(DateUtils.yymd)) + formattedDataType);
        detail.setSampleDate(report.getSampleDate());
        task.setReportName(report.getReportName());
        task.setReportCode(report.getReportCode());
        task.setMonitorCategory(report.getMonitorCategory());
        task.setSamplingPerson(report.getSamplingPerson());
        task.setSampleDate(report.getSampleDate());
        task.setAnalysisPerson(report.getAnalysisPerson());
        task.setReportDate(report.getReportDate());
        task.setContactPerson(report.getContactPerson());
        task.setContactPhone(report.getContactPhone());
        task.setAnalysisStartDate(report.getAnalysisStartDate());
        task.setAnalysisEndDate(report.getAnalysisEndDate());
        task.setOperationHour(detail.getOperationHour());
        task.setAnnexIds(report.getAnnexIds());// 报告的附件对所有任务公共
        JSONObject monitorData = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("max", detail.getMax());
        data.put("min", detail.getMin());
        data.put("avg", detail.getAvg());
        data.put("zsAvg", detail.getZsAvg());
        monitorData.put(task.getPollutantCode(), data);
        detail.setDataInfo(JSONObject.toJSONString(monitorData));
    }

    private String parseReportExcel(MultipartFile file, EnvManualCheckReport report) {
        String fileName = file.getOriginalFilename();
        if (null == fileName || fileName.isEmpty()) {
            return "导入文件名称为空";
        }
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!".xlsx".equals(fileType)) {
            return "不支持 " + fileType + " 文件类型";
        }
        List<EnvManualCheckReportDetail> detailList = new ArrayList<>();
        report.setDetailList(detailList);
        try {
            Workbook wb = new XSSFWorkbook(file.getInputStream());
            Sheet sheet0 = wb.getSheetAt(0);
            // 获取当前行的数据
            Row row = sheet0.getRow(0);
            if (null != row) {
                report.setReportName(CellUtils.getCellStringVal(row, 1));
                report.setReportCode(CellUtils.getCellStringVal(row, 3));
                report.setMonitorCategory(CellUtils.getCellStringVal(row, 5));
            }
            row = sheet0.getRow(1);
            if (null != row) {
                report.setSamplingPerson(CellUtils.getCellStringVal(row, 1));
                report.setSampleDate(CellUtils.getCellLocalDateVal(row, 3));
                report.setAnalysisPerson(CellUtils.getCellStringVal(row, 5));
            }
            row = sheet0.getRow(2);
            if (null != row) {
                report.setReportDate(CellUtils.getCellLocalDateVal(row, 1));
                report.setContactPerson(CellUtils.getCellStringVal(row, 3));
                report.setContactPhone(CellUtils.getCellStringVal(row, 5));
            }
            row = sheet0.getRow(3);
            if (null != row) {
                report.setAnalysisStartDate(CellUtils.getCellLocalDateVal(row, 1));
                report.setAnalysisEndDate(CellUtils.getCellLocalDateVal(row, 3));
            }
            // 过滤表头行，第一、二行
            Sheet sheet1 = wb.getSheetAt(1);
            for (int i = 2; i <= sheet1.getLastRowNum(); i++) {
                // 获取当前行的数据
                row = sheet1.getRow(i);
                if (null == row)
                    continue;
                int index = 1;// 略过序号列
                EnvManualCheckReportDetail detail = new EnvManualCheckReportDetail();
                String value = CellUtils.getCellStringVal(row, index++);
                if (null == value) {
                    // 排放口名称为空略过
                    continue;
                } else {
                    detail.setOutPutName(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null == value) {
                    // 监测因子为空略过
                    continue;
                } else {
                    detail.setPollutantCodeName(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null == value) {
                    // 检测频次为空略过
                    continue;
                } else {
                    detail.setCheckFrequencyDesc(value);
                }
                detail.setOperationHour(CellUtils.getCellIntegerVal(row, index++));
                // 略过最大最小限值
                index+=2;
                BigDecimal decimal = CellUtils.getCellBigDecimalVal(row, index++, 3);
                detail.setMax(decimal);
                decimal = CellUtils.getCellBigDecimalVal(row, index++, 3);
                detail.setMin(decimal);
                decimal = CellUtils.getCellBigDecimalVal(row, index++, 3);
                detail.setAvg(decimal);
                decimal = CellUtils.getCellBigDecimalVal(row, index, 3);
                detail.setZsAvg(decimal);

                detailList.add(detail);
            }
        } catch (Exception e) {
            return "导入文件解析失败";
        }
        return null;
    }

    public AjaxResult saveReport(EnvManualCheckReport report) {
        List<String> taskIdList = new ArrayList<>();
        report.getDetailList().forEach( e -> taskIdList.add(e.getTaskId()));
        // 校验数据准确性
        List<EnvManualCheckTask> taskList = null;
        if (!taskIdList.isEmpty()) {
            taskList = platformFacade.selectEnvManualCheckTaskForReport(taskIdList);
        }
        // k outPutId -> pollutantCode -> checkFrequency  v detail
        Map<String, EnvManualCheckTask> taskMap = new HashMap<>();
        if (null != taskList && !taskList.isEmpty()) {
            taskList.forEach( e -> taskMap.put(e.getTaskId(), e));
        }
        for (EnvManualCheckReportDetail detail : report.getDetailList()) {
            EnvManualCheckTask task = taskMap.get(detail.getTaskId());
            if (null == task) {
                return AjaxResult.error("任务 [" + detail.getTaskId() + "] 不存在或为不可上报数据状态");
            }
            setData(task, detail, report);
        }
        // 更新任务中的报告信息
        platformFacade.batchUpdateEnvManualCheckTask(taskList);
        // 报告数据入库
        int count = envManualCheckMapper.batchInsertEnvManualCheckData(report.getDetailList());
        return AjaxResult.success(count);
    }
}

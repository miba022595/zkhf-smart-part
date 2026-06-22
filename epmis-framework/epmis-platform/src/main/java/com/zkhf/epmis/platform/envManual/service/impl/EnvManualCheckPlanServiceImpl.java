package com.zkhf.epmis.platform.envManual.service.impl;

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
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckPlanReq;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import com.zkhf.epmis.platform.envManual.enums.PlanStatusType;
import com.zkhf.epmis.platform.envManual.service.EnvManualCheckPlanService;
import com.zkhf.epmis.platform.envManual.service.EnvManualCheckTaskService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envManual.EnvManualCheckPlanMapper;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 环境手工检测计划Service业务层处理
 */
@Slf4j
@Service("envManualCheckPlanService")
public class EnvManualCheckPlanServiceImpl implements EnvManualCheckPlanService, ApprovalService {

    private EnvManualCheckPlanMapper envManualCheckPlanMapper;
    @Autowired
    public void setEnvManualCheckPlanMapper(EnvManualCheckPlanMapper envManualCheckPlanMapper) {
        this.envManualCheckPlanMapper = envManualCheckPlanMapper;
    }

    private EnvManualCheckTaskService envManualCheckTaskService;
    @Autowired
    public void setEnvManualCheckTaskService(EnvManualCheckTaskService envManualCheckTaskService) {
        this.envManualCheckTaskService = envManualCheckTaskService;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectEnvManualCheckPlanList(EnvManualCheckPlanReq req) {
        // 请求参数转换
        req = initReq(req);
        // 分页查询
        PageUtils.startPage();
        List<EnvManualCheckPlan> list = envManualCheckPlanMapper.selectEnvManualCheckPlanList(req);
        // 数据填充
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "环境手工检测计划", businessType = BusinessType.EXPORT)
    public void exportEnvManualCheckPlan(EnvManualCheckPlanReq req, HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环境手工检测计划模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 请求参数转换
            req = initReq(req);
            List<EnvManualCheckPlan> list = envManualCheckPlanMapper.selectEnvManualCheckPlanList(req);
            if (null != list && !list.isEmpty()) {
                // 数据填充
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 从第（rowIndex + 1）行开始插入

                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN3 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,3);
                int index = 1;
                Row row;
                Cell cell;
                List<CellRangeAddress> mergeRegions = new ArrayList<>();

                int groupStartRow = rowIndex;
                int groupSize = 0;
                String prevOutPutId = null;

                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (int i = 0; i < list.size(); i++) {
                    EnvManualCheckPlan info = list.get(i);
                    if (null == info.getOutPutId()) {
                        continue;
                    }

                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;

                    // 判断是否是组的开始(标记只在组的起始行显示)
                    boolean isGroupStart = !info.getOutPutId().equals(prevOutPutId);
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(index);
                    }
                    // 排放口名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getOutPutName());
                    }
                    // 排放口类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(OutPutTypeEnum.getNameByCode(info.getOutPutType()));
                    }
                    // 其他列每行都显示
                    // 排口关联污染物code
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutantCode());
                    // 排口关联污染物code
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutantNameCn());
                    // 超标上限
                    cell = CellUtils.getCell(row, cellIndex++, styleN3);
                    if (null != info.getOverMaxvalue()) {
                        cell.setCellValue(info.getOverMaxvalue());
                    }
                    // 超标下限
                    cell = CellUtils.getCell(row, cellIndex++, styleN3);
                    if (null != info.getOverMinvalue()) {
                        cell.setCellValue(info.getOverMinvalue());
                    }
                    // 执行标准
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getExecutionStandard());
                    // 计划首次执行时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getFirstDate()) {
                        cell.setCellValue(info.getFirstDate().format(DateUtils.yy_m_d));
                    }
                    // 计划检测频次
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getCheckFrequencyDesc());
                    // 检测计划描述
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPlanDesc());
                    // 计划状态
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getStatusDesc());

                    groupSize++;

                    // 判断是否是组的结束
                    boolean isGroupEnd = (i == list.size() - 1) ||
                            !info.getOutPutId().equals(list.get(i + 1).getOutPutId());

                    if (isGroupEnd) {
                        // 添加合并区域（如果组有多行）
                        if (groupSize > 1) {
                            for (int col = 0; col <= 3; col++) {
                                mergeRegions.add(new CellRangeAddress(groupStartRow, groupStartRow + groupSize - 1, col, col));
                            }
                        }
                        // 重置组信息
                        groupStartRow = rowIndex;
                        groupSize = 0;
                        index++; // 序号递增
                    }
                    prevOutPutId = info.getOutPutId();
                }
                // 应用合并区域
                for (CellRangeAddress mergeRegion : mergeRegions) {
                    sheet.addMergedRegion(mergeRegion);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环境手工检测计划.xlsx", StandardCharsets.UTF_8));
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

    private EnvManualCheckPlanReq initReq(EnvManualCheckPlanReq req) {
        if (null == req) {
            req = new EnvManualCheckPlanReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        if (StringUtils.isNotEmpty(req.getFirstDateStart()) && StringUtils.isNotEmpty(req.getFirstDateEnd())) {
            req.setFirstDateStart(req.getFirstDateStart() + " 00:00:00");
            req.setFirstDateEnd(req.getFirstDateEnd() + " 23:59:59");
        } else {
            req.setFirstDateStart(null);
            req.setFirstDateEnd(null);
        }
        return req;
    }

    private void fill(List<EnvManualCheckPlan> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        for (EnvManualCheckPlan info : list) {
            info.setStatusDesc(PlanStatusType.getNameByCode(info.getStatus()));
            if (StringUtils.isEmpty(info.getCheckFrequency())) {
                continue;
            }
            StringBuilder bu = new StringBuilder();
            for (String item : info.getCheckFrequency().split(",")) {
                int code = StringUtils.strToInt(item, 0);
                bu.append("，").append(CheckFrequencyType.getNameByCode(code));
            }
            if (bu.length() > 0) {
                info.setCheckFrequencyDesc(bu.substring(1));
            }
        }
    }

    @Override
    @Log(title = "环境手工检测计划", businessType = BusinessType.INSERT)
    public AjaxResult insertEnvManualCheckPlan(EnvManualCheckPlan info) {
        if (StringUtils.isEmpty(info.getOutPutPollId())) {
            return AjaxResult.error("排口污染物不能为空");
        }
        // 默认草稿状态
        if (null == info.getStatus()) {
            info.setStatus(PlanStatusType.STATUS_CG.code);
        }
        // 可保存为草稿或待审批状态
        if (!PlanStatusType.STATUS_CG.code.equals(info.getStatus()) && !PlanStatusType.STATUS_DSP.code.equals(info.getStatus())) {
            return AjaxResult.error("新增时不支持的计划");
        }
        EnvManualCheckPlan oldPlan = envManualCheckPlanMapper.selectEnvManualCheckPlanByOutPutPollId(info.getOutPutPollId());
        if (null != oldPlan) {
            return AjaxResult.error("检测计划已配置");
        }
        int count = envManualCheckPlanMapper.insertEnvManualCheckPlan(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(info.getOutPutPollId(), AnnexTypeEnum.envManualCheckPlan.name(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环境手工检测计划", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvManualCheckPlan(EnvManualCheckPlan info) {
        EnvManualCheckPlan oldPlan = envManualCheckPlanMapper.selectEnvManualCheckPlanByOutPutPollId(info.getOutPutPollId());
        if (null == oldPlan) {
            return AjaxResult.error("检测计划不存在");
        }
        String errMsg = null;
        if (PlanStatusType.STATUS_SPZ.code.equals(oldPlan.getStatus())) {
            errMsg = "计划审批中，不可修改";
        } else if (PlanStatusType.STATUS_YQX.code.equals(oldPlan.getStatus())) {
            errMsg = "计划已取消，不允许修改";
        } else if (PlanStatusType.STATUS_YWC.code.equals(oldPlan.getStatus())) {
            errMsg = "计划已完成，不允许修改";
        } else if (PlanStatusType.STATUS_YZZ.code.equals(oldPlan.getStatus())) {
            errMsg = "计划已终止，不允许修改";
        } else if (PlanStatusType.STATUS_YSP.code.equals(info.getStatus())) {
            errMsg = "不可手动变更为已审批";
        }
        if (null != errMsg) {
            return AjaxResult.error(errMsg);
        }
        // 旧的状态未知则设置为草稿
        Integer oldStatus = oldPlan.getStatus();
        if (null == oldStatus) {
            oldStatus = PlanStatusType.STATUS_CG.code;
            info.setStatus(oldStatus);
        }
        // 草稿、待审批状态可变更为已取消、草稿、待审批
        if ((PlanStatusType.STATUS_CG.code.equals(oldStatus) || PlanStatusType.STATUS_DSP.code.equals(oldStatus)) &&
                (PlanStatusType.STATUS_YSP.code.equals(info.getStatus())
                        || PlanStatusType.STATUS_YWC.code.equals(info.getStatus())
                        || PlanStatusType.STATUS_YZZ.code.equals(info.getStatus()))) {
            return AjaxResult.error("状态不能越级变更 " + PlanStatusType.STATUS_DSP.name + " -》 " + PlanStatusType.getNameByCode(info.getStatus()));
        }
        // 已审批可变更为已完成、已终止
        if (PlanStatusType.STATUS_YSP.code.equals(oldStatus) && !PlanStatusType.STATUS_YWC.code.equals(info.getStatus())
                        && !PlanStatusType.STATUS_YZZ.code.equals(info.getStatus())) {
            return AjaxResult.error("状态不能越级变更 " + PlanStatusType.STATUS_DSP.name + " -》 " + PlanStatusType.getNameByCode(info.getStatus()));
        }
        int result = envManualCheckPlanMapper.updateEnvManualCheckPlan(info);
        // 修改附件信息
        if (result > 0) {
            annexService.updateAnnex(info.getOutPutPollId(), AnnexTypeEnum.envManualCheckPlan.name(), info.getAnnexIds());
            // 任务初始化生成
            envManualCheckTaskService.initTask(oldStatus, info);
        }
        return AjaxResult.success(result);
    }

    @Override
    @Log(title = "环境手工检测计划", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnvManualCheckPlanByOutPutPollId(String outPutPollId) {
        EnvManualCheckPlan oldPlan = envManualCheckPlanMapper.selectEnvManualCheckPlanByOutPutPollId(outPutPollId);
        if (null != oldPlan && !oldPlan.getStatus().equals(PlanStatusType.STATUS_CG.code)) {
            return AjaxResult.error("只能删除草稿状态下的数据");
        }
        int result = envManualCheckPlanMapper.deleteEnvManualCheckPlanByOutPutPollId(outPutPollId);
        if (result > 0) {
            // 删除附件
            annexService.updateAnnex(outPutPollId, AnnexTypeEnum.envManualCheckPlan.name(), null);
        }
        return AjaxResult.success(result);
    }

    @Async
    @Override
    public void approval(ApprovalInstance instance) {
        if (instance == null) {
            return;
        }
        EnvManualCheckPlan plan = new EnvManualCheckPlan();
        Integer newStatus;
        if (ApprovalInstanceStatus.PROCESSING.code.equals(instance.getStatus())) {
            newStatus = PlanStatusType.STATUS_SPZ.code;
        } else if (ApprovalInstanceStatus.APPROVED.code.equals(instance.getStatus())) {
            newStatus = PlanStatusType.STATUS_YSP.code;
        } else if (ApprovalInstanceStatus.REJECTED.code.equals(instance.getStatus())) {
            newStatus = PlanStatusType.STATUS_YJJ.code;
        } else if (ApprovalInstanceStatus.CANCELLED.code.equals(instance.getStatus())) {
            newStatus = PlanStatusType.STATUS_YQX.code;
        } else {
            return;
        }
        plan.setOutPutPollId(instance.getBusinessKey());
        plan.setApprovalOpinion(instance.getComment());
        plan.setStatus(newStatus);
        envManualCheckPlanMapper.updateEnvManualCheckPlan(plan);
    }
}

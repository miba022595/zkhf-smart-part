package com.zkhf.epmis.platform.envProtect.policy.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupport;
import com.zkhf.epmis.platform.envProtect.policy.domain.GovernFundSupportReq;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchActual;
import com.zkhf.epmis.platform.envProtect.policy.domain.SupportBatchPlan;
import com.zkhf.epmis.platform.envProtect.policy.service.GovernFundSupportService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.policy.GovernFundSupportMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 政府资金支持Service业务层处理
 */
@Slf4j
@Service
public class GovernFundSupportServiceImpl implements GovernFundSupportService {

    private GovernFundSupportMapper governFundSupportMapper;

    @Autowired
    public void setGovernFundSupportMapper(GovernFundSupportMapper governFundSupportMapper) {
        this.governFundSupportMapper = governFundSupportMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectGovernFundSupportList(GovernFundSupportReq req) {
        if (null == req) {
            req = new GovernFundSupportReq();
        }
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<GovernFundSupport> list = governFundSupportMapper.selectGovernFundSupportList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "政府资金支持", businessType = BusinessType.EXPORT)
    public void exportGovernFundSupport(GovernFundSupportReq req, HttpServletResponse response) {
        if (null == req) {
            req = new GovernFundSupportReq();
        }
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("政府资金支持列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 查询列表
            List<GovernFundSupport> list = governFundSupportMapper.selectGovernFundSupportList(req);
            // 查询计划列表
            List<SupportBatchPlan> planList = governFundSupportMapper.selectGovernFundSupportPList(req);
            // 查询实际列表
            List<SupportBatchActual> actualList = governFundSupportMapper.selectGovernFundSupportAList(req);
            Map<String, String> funIdName = new HashMap<>();
            if (null != list && !list.isEmpty()) {
                list.forEach(e -> funIdName.put(e.getSupportId(), e.getProjectName()));
            }
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行，从第4行开始插入

                // 获取数字单元格格式
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                CellStyle numStyle4 = CellUtils.getCellStyle(workbook, sheet, rowIndex,0,4);
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (GovernFundSupport fund : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 支持项目名称
                    CellUtils.setStringVal(row, cellIndex++, fund.getProjectName(), style);
                    // 投资金额(万)
                    CellUtils.setDoubleVal(row, cellIndex++, fund.getInvestmentAmount(), numStyle4);
                    // 负责人
                    CellUtils.setStringVal(row, cellIndex++, fund.getProName(), style);
                    // 项目所在单位
                    CellUtils.setStringVal(row, cellIndex++, fund.getEntName(), style);
                    // 项目内容
                    CellUtils.setStringVal(row, cellIndex++, fund.getProjectContent(), style);
                    // 减排效果
                    CellUtils.setStringVal(row, cellIndex++, fund.getReduceEffect(), style);
                    // 外部立项-计划时间
                    CellUtils.setLocalDateStr(row, cellIndex++, fund.getEpsTime(), DateUtils.yy_m_d, style);
                    // 外部立项-实际时间
                    CellUtils.setLocalDateStr(row, cellIndex++, fund.getEpaTime(), DateUtils.yy_m_d, style);
                    // 项目完成-计划时间
                    CellUtils.setLocalDateStr(row, cellIndex++, fund.getPcsTime(), DateUtils.yy_m_d, style);
                    // 项目完成-实际时间
                    CellUtils.setLocalDateStr(row, cellIndex++, fund.getPcaTime(), DateUtils.yy_m_d, style);
                    // 政府资金支持金额(万)
                    CellUtils.setDoubleVal(row, cellIndex++, fund.getSupportAmount(), numStyle4);
                    // 下发支持资金部门
                    CellUtils.setStringVal(row, cellIndex, fund.getSendDept(), style);
                }
                // 写入计划列表信息
                setExcelPlanList(planList, funIdName, workbook.getSheetAt(1), workbook);
                // 写入实际列表信息
                setExcelActualList(actualList, funIdName, workbook.getSheetAt(2), workbook);
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("政府资金支持列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
        }
    }

    /**
     * 写入计划列表信息
     */
    private void setExcelPlanList(List<SupportBatchPlan> planList, Map<String, String> funIdName, Sheet sheet, XSSFWorkbook workbook) {
        if (null == planList || planList.isEmpty()) {
            return;
        }
        try {
            int rowIndex = 1;// 首行，从第2行开始插入
            // 获取数字单元格格式
            CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
            // 获取单元格格式
            CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
            int index = 1;
            Row row;
            // 行移动（获取单元格样式之后）
            CellUtils.shiftRows(sheet, rowIndex, planList.size());
            for (SupportBatchPlan plan : planList) {
                row = sheet.createRow(rowIndex++);

                int cellIndex = 0;
                // 序号
                CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                // 支持项目名称
                CellUtils.setStringVal(row, cellIndex++, funIdName.get(plan.getSupportId()), style);
                // 计划批次名称
                CellUtils.setStringVal(row, cellIndex++, plan.getPlanName(), style);
                // 批次比例/%
                CellUtils.setIntegerVal(row, cellIndex++, plan.getPlanRate(), style0);
                // 下发时间
                CellUtils.setLocalDateStr(row, cellIndex, plan.getSendTime(), DateUtils.yy_m_d, style);
            }
        } catch (Exception e) {
            log.error("计划列表信息写入失败", e);
        }
    }

    /**
     * 写入实际列表信息
     */
    private void setExcelActualList(List<SupportBatchActual> actualList, Map<String, String> funIdName, Sheet sheet, XSSFWorkbook workbook) {
        if (null == actualList || actualList.isEmpty()) {
            return;
        }
        try {
            int rowIndex = 1;// 首行，从第2行开始插入

            // 获取数字单元格格式
            CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
            CellStyle numStyle4 = CellUtils.getCellStyle(workbook, sheet, rowIndex,0,4);
            // 获取单元格格式
            CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
            int index = 1;
            Row row;
            // 行移动（获取单元格样式之后）
            CellUtils.shiftRows(sheet, rowIndex, actualList.size());
            for (SupportBatchActual actual : actualList) {
                row = sheet.createRow(rowIndex++);
                int cellIndex = 0;
                // 序号
                CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                // 支持项目名称
                CellUtils.setStringVal(row, cellIndex++, funIdName.get(actual.getSupportId()), style);
                // 到账金额(万)
                CellUtils.setDoubleVal(row, cellIndex++, actual.getActualAmount(), numStyle4);
                // 到账时间
                CellUtils.setLocalDateStr(row, cellIndex, actual.getActualTime(), DateUtils.yy_m_d, style);
            }
        } catch (Exception e) {
            log.error("实际列表信息写入失败", e);
        }
    }

    @Override
    public AjaxResult selectSupportDetail(String supportId) {
        AjaxResult result = AjaxResult.success();
        result.put("planList", governFundSupportMapper.selectSupportBatchPlanList(supportId));
        result.put("actualList", governFundSupportMapper.selectSupportBatchActualList(supportId));
        return result;
    }

    @Override
    @Log(title = "政府资金支持", businessType = BusinessType.INSERT)
    public AjaxResult insertGovernFundSupport(GovernFundSupport info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setSupportId(UlidCreator.getMonotonicUlid().toString());
        int count = governFundSupportMapper.insertGovernFundSupport(info);
        if (count > 0) {
            // 附件处理
            if (null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getSupportId(), AnnexTypeEnum.governFundSupport.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "政府资金支持-计划批次", businessType = BusinessType.INSERT)
    public AjaxResult insertGovernFundSupportP(SupportBatchPlan plan) {
        governFundSupportMapper.insertGovernFundSupportP(plan);
        return AjaxResult.success(plan);
    }

    @Override
    @Log(title = "政府资金支持-实际批次", businessType = BusinessType.INSERT)
    public AjaxResult insertGovernFundSupportA(SupportBatchActual actual) {
        governFundSupportMapper.insertGovernFundSupportA(actual);
        return AjaxResult.success(actual);
    }

    @Override
    @Log(title = "政府资金支持", businessType = BusinessType.UPDATE)
    public AjaxResult updateGovernFundSupport(GovernFundSupport info) {
        int count = governFundSupportMapper.updateGovernFundSupport(info);
        if (count > 0) {
            // 附件处理
            if (null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getSupportId(), AnnexTypeEnum.governFundSupport.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "政府资金支持-计划批次", businessType = BusinessType.UPDATE)
    public AjaxResult updateGovernFundSupportP(SupportBatchPlan plan) {
        governFundSupportMapper.updateGovernFundSupportP(plan);
        return AjaxResult.success();
    }

    @Override
    @Log(title = "政府资金支持-实际批次", businessType = BusinessType.UPDATE)
    public AjaxResult updateGovernFundSupportA(SupportBatchActual actual) {
        governFundSupportMapper.updateGovernFundSupportA(actual);
        return AjaxResult.success();
    }

    @Override
    @Log(title = "政府资金支持", businessType = BusinessType.DELETE)
    public AjaxResult deleteGovernFundSupportBySupportId(String supportId) {
        int count = governFundSupportMapper.deleteGovernFundSupportBySupportId(supportId);
        if (count > 0) {
            // 删除对应的计划批次
            governFundSupportMapper.deleteGovernFundSupportPBySupportId(supportId);
            // 删除对应的实际批次
            governFundSupportMapper.deleteGovernFundSupportABySupportId(supportId);
            // 删除附件
            annexService.updateAnnex(supportId, AnnexTypeEnum.governFundSupport.name(), null);
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "政府资金支持-计划批次", businessType = BusinessType.DELETE)
    public AjaxResult deleteGovernFundSupportPById(Long id) {
        governFundSupportMapper.deleteGovernFundSupportPById(id);
        return AjaxResult.success();
    }

    @Override
    @Log(title = "政府资金支持-实际批次", businessType = BusinessType.DELETE)
    public AjaxResult deleteGovernFundSupportAById(Long id) {
        governFundSupportMapper.deleteGovernFundSupportAById(id);
        return AjaxResult.success();
    }
}


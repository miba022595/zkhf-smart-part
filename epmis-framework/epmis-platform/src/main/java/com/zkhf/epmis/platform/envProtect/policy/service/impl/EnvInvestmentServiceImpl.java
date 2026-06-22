package com.zkhf.epmis.platform.envProtect.policy.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestment;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvInvestmentReq;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvInvestmentService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.policy.EnvInvestmentMapper;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 环保法规与体系管理-环保投入Service业务层处理
 */
@Slf4j
@Service
public class EnvInvestmentServiceImpl implements EnvInvestmentService {

    private EnvInvestmentMapper envInvestmentMapper;

    @Autowired
    public void setEnvInvestmentMapper(EnvInvestmentMapper envInvestmentMapper) {
        this.envInvestmentMapper = envInvestmentMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectEnvInvestmentList(EnvInvestmentReq req) {
        AjaxResult result = AjaxResult.success();
        if (null == req) {
            req = new EnvInvestmentReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        long count = envInvestmentMapper.selectEnvInvestmentCount(req);
        result.put("total", count);
        List<EnvInvestment> list;
        if (count > 0) {
            req.setPageSize(PageUtils.getPageSize(10));
            // 跳页处理
            skipPageProcess(req, count);
            list = envInvestmentMapper.selectEnvInvestmentList(req);
        } else {
            list = new ArrayList<>();
        }
        result.put("data", list);
        return result;
    }

    /**
     * 跳页处理
     */
    private void skipPageProcess(EnvInvestmentReq req, long count) {
        // 判断是否跳页
        int pageNum = PageUtils.getPageNum(1);
        if (count <= req.getPageSize()) { // 总量只够一页的不需要处理
            req.setInvestmentIdF(null);
            req.setInvestmentIdE(null);
            return;
        }
        // 非首页，且无本页的第一或最后一条参数时，当作跳页处理
        if (pageNum > 1 && StringUtils.isEmpty(req.getInvestmentIdF()) && StringUtils.isEmpty(req.getInvestmentIdE())) {
            // 偏移量设置
            req.setOffset(PageUtils.getOffset(pageNum, req.getPageSize(), count));
            String investmentIdE = envInvestmentMapper.selectSkipPageSign(req);
            req.setInvestmentIdE(investmentIdE);
        }
    }

    @Override
    @Log(title = "环保法规与体系管理-环保投入列表", businessType = BusinessType.EXPORT)
    public void exportEnvInvestment(EnvInvestmentReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvInvestmentReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保投入列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvInvestment> list = envInvestmentMapper.selectEnvInvestmentListForExport(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3; // 首行

                // 获取数字单元格格式
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                CellStyle style4 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 4);
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvInvestment info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 项目名称
                    CellUtils.setStringVal(row, cellIndex++, info.getProjectName(), style);
                    // 投资金额
                    CellUtils.setDoubleVal(row, cellIndex++, info.getInvestmentAmount(), style4);
                    // 负责人
                    CellUtils.setStringVal(row, cellIndex++, info.getProName(), style);
                    // 项目所在单位
                    CellUtils.setStringVal(row, cellIndex++, info.getEntName(), style);
                    // 项目内容
                    CellUtils.setStringVal(row, cellIndex++, info.getInvestmentDesc(), style);
                    // 减排效果
                    CellUtils.setStringVal(row, cellIndex++, info.getReductionEffect(), style);
                    // 内部立项时间-计划时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getPipaTime(), DateUtils.yy_m_d, style);
                    // 内部立项时间-实际时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getAipaTime(), DateUtils.yy_m_d, style);
                    // 施工入厂时间-计划时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getPceTime(), DateUtils.yy_m_d, style);
                    // 施工入厂时间-实际时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getAceTime(), DateUtils.yy_m_d, style);
                    // 完成时间-计划时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getPcTime(), DateUtils.yy_m_d, style);
                    // 完成时间-实际时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getAfTime(), DateUtils.yy_m_d, style);
                    // 验收时间-计划时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getPaTime(), DateUtils.yy_m_d, style);
                    // 验收时间-实际时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getAcTime(), DateUtils.yy_m_d, style);
                    // 是否取得政府资金支持
                    CellUtils.setStringVal(row, cellIndex, null != info.getGovernmentFund() && 1 == info.getGovernmentFund() ? "是" : "否", style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保投入列表.xlsx", StandardCharsets.UTF_8));
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

    @Override
    public AjaxResult insertEnvInvestment(EnvInvestment info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setInvestmentId(UlidCreator.getMonotonicUlid().toString());
        info.setCreateTime(LocalDateTime.now());
        int count = envInvestmentMapper.insertEnvInvestment(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(info.getInvestmentId(), AnnexTypeEnum.envInvestment.name(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult updateEnvInvestment(EnvInvestment info) {
        info.setUpdateTime(LocalDateTime.now());
        int count = envInvestmentMapper.updateEnvInvestment(info);
        if (count > 0) {
            annexService.updateAnnex(info.getInvestmentId(), AnnexTypeEnum.envInvestment.name(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult deleteEnvInvestmentByInvestmentId(String investmentId) {
        int count = envInvestmentMapper.deleteEnvInvestmentByInvestmentId(investmentId);
        if (count > 0) {
            // 删除附件
            annexService.updateAnnex(investmentId, AnnexTypeEnum.envInvestment.name(), null);
        }
        return AjaxResult.success(count);
    }
}


package com.zkhf.epmis.platform.envProtect.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeCheck;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeRelate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;
import com.zkhf.epmis.platform.envProtect.service.EnvMangeCheckService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.EnvMangeCheckMapper;
import com.zkhf.epmis.platform.mapper.envProtect.EnvMangeRelateMapper;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 企业环评环保管理-环保验收Service业务层处理
 */
@Slf4j
@Service
public class EnvMangeCheckServiceImpl implements EnvMangeCheckService {

    private EnvMangeCheckMapper envMangeCheckMapper;

    @Autowired
    public void setEnvMangeCheckMapper(EnvMangeCheckMapper envMangeCheckMapper) {
        this.envMangeCheckMapper = envMangeCheckMapper;
    }

    private EnvMangeRelateMapper envMangeRelateMapper;

    @Autowired
    public void setEnvMangeProjectRelateMapper(EnvMangeRelateMapper envMangeRelateMapper) {
        this.envMangeRelateMapper = envMangeRelateMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult checkRelateList(String checkId) {
        return AjaxResult.success(envMangeRelateMapper.selectRelateByRelateId(checkId, EnvMangeRelate.RELATE_YS));
    }

    @Override
    public AjaxResult selectMangeCheckList(EnvMangeReq req) {
        if (null == req) {
            req = new EnvMangeReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        boolean page = PageUtils.startPageCheckExists();
        List<EnvMangeCheck> list = envMangeCheckMapper.selectMangeCheckList(req);
        return PageUtils.getAjaxResult(list, page);
    }


    @Override
    @Log(title = "企业环评环保管理-环保验收", businessType = BusinessType.EXPORT)
    public void exportMangeCheck(EnvMangeReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvMangeReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保验收列表.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvMangeCheck> list = envMangeCheckMapper.selectMangeCheckList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvMangeCheck check : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, check.getEntName(), style);
                    // 环保验收名称
                    CellUtils.setStringVal(row, cellIndex++, check.getCheckName(), style);
                    // 是否需验收批复，0否，1是
                    CellUtils.setStringVal(row, cellIndex++, EnvMangeCheck.CHECK_REPLY_Y.equals(check.getCheckReply()) ? "是" : "否", style);
                    // 审批部门
                    CellUtils.setStringVal(row, cellIndex++, check.getApprovalDepart(), style);
                    // 批复文号
                    CellUtils.setStringVal(row, cellIndex++, check.getReplyNo(), style);
                    // 验收监测机构
                    CellUtils.setStringVal(row, cellIndex++, check.getCheckAgency(), style);
                    // 验收监测时间-开始时间
                    CellUtils.setLocalDateStr(row, cellIndex++, check.getCheckBeginTime(), DateUtils.yy_m_d, style);
                    // 验收监测时间-结束时间
                    CellUtils.setLocalDateStr(row, cellIndex++, check.getCheckEndTime(), DateUtils.yy_m_d, style);
                    // 验收报告专家评审时间
                    CellUtils.setLocalDateStr(row, cellIndex++, check.getReviewTime(), DateUtils.yy_m_d, style);
                    // 验收报告专家评审主要问题
                    CellUtils.setStringVal(row, cellIndex++, check.getReviewIssue(), style);
                    // 验收报告公式地址
                    CellUtils.setStringVal(row, cellIndex++, check.getRecordAddress(), style);
                    // 验收报告公式开始时间
                    CellUtils.setLocalDateStr(row, cellIndex++, check.getCheckRecordBeginTime(), DateUtils.yy_m_d, style);
                    // 验收报告公式截至时间
                    CellUtils.setLocalDateStr(row, cellIndex++, check.getCheckRecordEndTime(), DateUtils.yy_m_d, style);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, check.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保验收列表.xlsx", StandardCharsets.UTF_8));
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
    @Log(title = "企业环保验收", businessType = BusinessType.INSERT)
    public AjaxResult insertMangeCheck(EnvMangeCheck info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setCheckId(UlidCreator.getMonotonicUlid().toString());
        int count = envMangeCheckMapper.insertMangeCheck(info);
        if (count > 0) {
            // 修改关联关系
            updateRelate(info);
            // 更新附件
            if (null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getCheckId(), AnnexTypeEnum.entEnvMangeCheck.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业环保验收", businessType = BusinessType.UPDATE)
    public AjaxResult updateMangeCheck(EnvMangeCheck info) {
        int count = envMangeCheckMapper.updateMangeCheck(info);
        if (count > 0) {
            // 修改关联关系
            updateRelate(info);
            annexService.updateAnnex(info.getCheckId(), AnnexTypeEnum.entEnvMangeCheck.name(), info.getAnnexIds());
        }
        return AjaxResult.success();
    }

    private void updateRelate(EnvMangeCheck info) {
        // 删除关联关系
        envMangeRelateMapper.deleteRelate(null, EnvMangeRelate.RELATE_YS, info.getCheckId());
        // 添加新的关联关系
        List<EnvMangeRelate> relateList = new ArrayList<>();
        if (null != info.getProjectIdList() && !info.getProjectIdList().isEmpty()) {
            info.getProjectIdList().forEach( e -> relateList.add(EnvMangeRelate.builder()
                    .projectId(e)
                    .relateType(EnvMangeRelate.RELATE_YS)
                    .relateId(info.getCheckId())
                    .build()));
        }
        if (!relateList.isEmpty()) {
            // 添加新的
            envMangeRelateMapper.batchInsertRelate(relateList);
        }
    }

    @Override
    @Log(title = "企业环保验收", businessType = BusinessType.DELETE)
    public AjaxResult deleteMangeCheckById(String id) {
        if (StringUtils.isEmpty(id)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = envMangeCheckMapper.deleteMangeCheckById(id);
        if (count > 0) {
            // 删除关联关系
            envMangeRelateMapper.deleteRelate(null, EnvMangeRelate.RELATE_YS, id);
            // 删除附件
            annexService.updateAnnex(id, AnnexTypeEnum.entEnvMangeCheck.name(), null);
        }
        return AjaxResult.success(count);
    }
}


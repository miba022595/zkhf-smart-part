package com.zkhf.epmis.platform.envProtect.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.service.PollutantCodeService;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeEvaluate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeRelate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;
import com.zkhf.epmis.platform.envProtect.service.EnvMangeEvaluateService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.EnvMangeEvaluateMapper;
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
import java.util.Map;

/**
 * 企业环评环保管理-环评Service业务层处理
 */
@Slf4j
@Service
public class EnvMangeEvaluateServiceImpl implements EnvMangeEvaluateService {

    private EnvMangeEvaluateMapper envMangeEvaluateMapper;

    @Autowired
    public void setEnvMangeEvaluateMapper(EnvMangeEvaluateMapper envMangeEvaluateMapper) {
        this.envMangeEvaluateMapper = envMangeEvaluateMapper;
    }

    private EnvMangeRelateMapper envMangeRelateMapper;

    @Autowired
    public void setEnvMangeProjectRelateMapper(EnvMangeRelateMapper envMangeRelateMapper) {
        this.envMangeRelateMapper = envMangeRelateMapper;
    }

    private PollutantCodeService pollutantCodeService;
    @Autowired
    public void setPollutantCodeService(PollutantCodeService pollutantCodeService) {
        this.pollutantCodeService = pollutantCodeService;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult evaluateRelateList(String evaluateId) {
        return AjaxResult.success(envMangeRelateMapper.selectRelateByRelateId(evaluateId, EnvMangeRelate.RELATE_HP));
    }

    @Override
    public AjaxResult selectMangeEvaluateList(EnvMangeReq req) {
        if (null == req) {
            req = new EnvMangeReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        boolean page = PageUtils.startPageCheckExists();
        List<EnvMangeEvaluate> list = envMangeEvaluateMapper.selectMangeEvaluateList(req);
        // 设置污染物信息
        setPollutantCodeDesc(list);
        return PageUtils.getAjaxResult(list, page);
    }


    @Override
    @Log(title = "企业环评环保管理-环评", businessType = BusinessType.EXPORT)
    public void exportMangeEvaluate(EnvMangeReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvMangeReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环评列表.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvMangeEvaluate> list = envMangeEvaluateMapper.selectMangeEvaluateList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                CellStyle style3 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 3);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvMangeEvaluate evaluate : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getEntName(), style);
                    // 环评名称
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getEvaluateName(), style);
                    // 环评层级
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getEiaLevel(), style);
                    // 审批部门
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getApprovalDepart(), style);
                    // 批复文号
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getReplyNo(), style);
                    // 评价机构
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getRatingAgency(), style);
                    // 主笔人员
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getLeadAuthor(), style);
                    // 评价费用(元)
                    CellUtils.setIntegerVal(row, cellIndex++, evaluate.getRatingCost(), style0);
                    // 主要污染物
                    CellUtils.setStringVal(row, cellIndex++, evaluate.getPollutantCodeDesc(), style);
                    // 污染物总量(kg)
                    CellUtils.setDoubleVal(row, cellIndex++, evaluate.getPollutantTotal(), style3);
                    // 合同签订时间
                    CellUtils.setLocalDateStr(row, cellIndex++, evaluate.getContractTime(), DateUtils.yy_m_d, style);
                    // 报告提交时间
                    CellUtils.setLocalDateStr(row, cellIndex++, evaluate.getReportSubTime(), DateUtils.yy_m_d, style);
                    // 对外公示时间
                    CellUtils.setLocalDateStr(row, cellIndex++, evaluate.getPublicityTime(), DateUtils.yy_m_d, style);
                    // 批复时间
                    CellUtils.setLocalDateStr(row, cellIndex++, evaluate.getApprovalTime(), DateUtils.yy_m_d, style);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, evaluate.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环评列表.xlsx", StandardCharsets.UTF_8));
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

    /**
     * 设置污染物信息
     */
    private void setPollutantCodeDesc(List<EnvMangeEvaluate> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 设置污染物名称
        Map<String, String> codeMap = pollutantCodeService.selectPollutantCodeName();
        list.forEach(e -> {
            e.setPollutantCodeDesc(null);
            if (StringUtils.isNotEmpty(e.getPollutantCode())) {
                for (String s : e.getPollutantCode().split(",")) {
                    if (null == e.getPollutantCodeDesc()) {
                        e.setPollutantCodeDesc(codeMap.get(s));
                    } else {
                        e.setPollutantCodeDesc(e.getPollutantCodeDesc() + "," + codeMap.get(s));
                    }
                }
            }
        });
    }

    @Override
    @Log(title = "企业环评", businessType = BusinessType.INSERT)
    public AjaxResult insertMangeEvaluate(EnvMangeEvaluate info) {
        info.setEvaluateId(UlidCreator.getMonotonicUlid().toString());
        int count = envMangeEvaluateMapper.insertMangeEvaluate(info);
        if (count > 0) {
            // 修改关联关系
            updateRelate(info);
            if (null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getEvaluateId(), AnnexTypeEnum.entEnvMangeEvaluate.name(), info.getAnnexIds());
            }
            if (null != info.getEvaluatePAnnexIds() && !info.getEvaluatePAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getEvaluateId(), AnnexTypeEnum.entEnvMangeEvaluateP.name(), info.getEvaluatePAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业环评", businessType = BusinessType.UPDATE)
    public AjaxResult updateMangeEvaluate(EnvMangeEvaluate info) {
        int count = envMangeEvaluateMapper.updateMangeEvaluate(info);
        if (count > 0) {
            // 修改关联关系
            updateRelate(info);
            annexService.updateAnnex(info.getEvaluateId(), AnnexTypeEnum.entEnvMangeEvaluate.name(), info.getAnnexIds());
            annexService.updateAnnex(info.getEvaluateId(), AnnexTypeEnum.entEnvMangeEvaluateP.name(), info.getEvaluatePAnnexIds());
        }
        return AjaxResult.success();
    }

    private void updateRelate(EnvMangeEvaluate info) {
        // 删除关联关系
        envMangeRelateMapper.deleteRelate(null, EnvMangeRelate.RELATE_HP, info.getEvaluateId());
        // 添加新的关联关系
        List<EnvMangeRelate> relateList = new ArrayList<>();
        if (null != info.getProjectIdList() && !info.getProjectIdList().isEmpty()) {
            info.getProjectIdList().forEach( e -> relateList.add(EnvMangeRelate.builder()
                    .projectId(e)
                    .relateType(EnvMangeRelate.RELATE_HP)
                    .relateId(info.getEvaluateId())
                    .build()));
        }
        if (!relateList.isEmpty()) {
            // 添加新的
            envMangeRelateMapper.batchInsertRelate(relateList);
        }
    }

    @Override
    @Log(title = "企业环评", businessType = BusinessType.DELETE)
    public AjaxResult deleteMangeEvaluateById(String id) {
        if (StringUtils.isEmpty(id)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = envMangeEvaluateMapper.deleteMangeEvaluateById(id);
        if (count > 0) {
            // 删除关联关系
            envMangeRelateMapper.deleteRelate(null, EnvMangeRelate.RELATE_HP, id);
            // 删除附件
            annexService.updateAnnex(id, AnnexTypeEnum.entEnvMangeEvaluate.name(), null);
            annexService.updateAnnex(id, AnnexTypeEnum.entEnvMangeEvaluateP.name(), null);
        }
        return AjaxResult.success(count);
    }
}


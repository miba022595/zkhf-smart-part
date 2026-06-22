package com.zkhf.epmis.platform.emergency.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlan;
import com.zkhf.epmis.platform.emergency.domain.EmergencyPlanReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyPlanService;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.emergency.EmergencyPlanMapper;
import com.zkhf.epmis.platform.mapper.ent.EnterpriseMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class EmergencyPlanServiceImpl implements EmergencyPlanService {

    private EmergencyPlanMapper emergencyPlanMapper;
    @Autowired
    public void setEmergencyPlanMapper(EmergencyPlanMapper emergencyPlanMapper) {
        this.emergencyPlanMapper = emergencyPlanMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private EnterpriseMapper enterpriseMapper;
    @Autowired
    public void setEnterpriseMapper(EnterpriseMapper enterpriseMapper) {
        this.enterpriseMapper = enterpriseMapper;
    }

    @Override
    public AjaxResult list(EmergencyPlanReq req) {
        // 请求参数转换
        req = initEmergencyPlanReq(req);
        if (null == req) {
            return AjaxResult.success();
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EmergencyPlan> list = emergencyPlanMapper.selectList(req);
        // 填充附件信息
        fillAnnexInfo(list);
        return PageUtils.getAjaxResult(list, page);
    }

    private void fillAnnexInfo(List<EmergencyPlan> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<String> sourceIds = list.stream().map(EmergencyPlan::getPlanId).toList();
        AnnexReq annexReq = new AnnexReq();
        annexReq.setSourceIds(sourceIds);
        annexReq.setSourceType(AnnexTypeEnum.emergencyPlanAttachment.name());
        List<AnnexInfo> annexList = annexService.selectAnnexList(annexReq);
        if (null != annexList && !annexList.isEmpty()) {
            Map<String, List<AnnexInfo>> annexMap = new HashMap<>();
            for (AnnexInfo annex : annexList) {
                annexMap.computeIfAbsent(annex.getSourceId(), k -> new ArrayList<>()).add(annex);
            }
            for (EmergencyPlan plan : list) {
                plan.setAnnexList(annexMap.get(plan.getPlanId()));
            }
        }
    }

    private EmergencyPlanReq initEmergencyPlanReq(EmergencyPlanReq req) {
        if (null == req) {
            req = new EmergencyPlanReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            List<String> authEntCodes = GVarContainer.getEntCodes();
            if (null == authEntCodes || authEntCodes.isEmpty()) {
                return null;
            }
            if (StringUtils.isNotBlank(req.getEntCode())) {
                if (!authEntCodes.contains(req.getEntCode())) {
                    return null;
                }
            } else {
                req.setEntCodes(authEntCodes);
            }
        }
        return req;
    }

    @Override
    @Log(title = "应急预案", businessType = BusinessType.INSERT)
    public AjaxResult add(EmergencyPlan info) {
        info.setPlanId(UlidCreator.getMonotonicUlid().toString());
        int rows = emergencyPlanMapper.insert(info);
        if (rows > 0 && info.getAnnexIds() != null && info.getAnnexIds().size() > 0) {
            annexService.updateAnnex(info.getPlanId(), AnnexTypeEnum.emergencyPlanAttachment.name(), info.getAnnexIds());
        }
        return AjaxResult.success(rows);
    }

    @Override
    @Log(title = "应急预案", businessType = BusinessType.UPDATE)
    public AjaxResult update(EmergencyPlan info) {
        EmergencyPlan plan = emergencyPlanMapper.selectById(info.getPlanId());
        if (plan == null) {
            return AjaxResult.error("数据不存在");
        }
        int rows = emergencyPlanMapper.update(info);
        if (rows > 0) {
            annexService.updateAnnex(info.getPlanId(), AnnexTypeEnum.emergencyPlanAttachment.name(), info.getAnnexIds());
        }
        return AjaxResult.success(rows);
    }

    @Override
    @Log(title = "应急预案", businessType = BusinessType.DELETE)
    public AjaxResult delete(EmergencyPlan info) {
        int rows = emergencyPlanMapper.deleteById(info.getPlanId());
        if (rows > 0) {
            annexService.updateAnnex(info.getPlanId(), AnnexTypeEnum.emergencyPlanAttachment.name(), null);
        }
        return AjaxResult.success(rows);
    }

    @Override
    public void export(EmergencyPlanReq req, HttpServletResponse response) {
        req = initEmergencyPlanReq(req);
        if (req == null) {
            return;
        }
        List<EmergencyPlan> list = emergencyPlanMapper.selectList(req);
        try (XSSFWorkbook workbook = ExcelUtils.getSheetAt("应急预案导入模板.xlsx")) {
            if (workbook == null) {
                return;
            }
            Sheet sheet = workbook.getSheetAt(0);
            if (list != null && !list.isEmpty()) {
                int rowIndex = 3;
                int index = 1;
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EmergencyPlan item : list) {
                    Row row = sheet.createRow(rowIndex++);
                    int cellIndex = 0;
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    String entName = item.getEntName() == null || item.getEntName().isBlank() ? item.getEntCode() : item.getEntName();
                    CellUtils.setStringVal(row, cellIndex++, entName, style);
                    CellUtils.setStringVal(row, cellIndex++, item.getPlanName(), style);
                    CellUtils.setStringVal(row, cellIndex++, item.getVersion(), style);
                    CellUtils.setStringVal(row, cellIndex++, item.getRiskUnit(), style);
                    CellUtils.setStringVal(row, cellIndex++, item.getHandlePoints(), style);
                    String planTypeText = "";
                    if (item.getPlanType() != null) {
                        planTypeText = switch (item.getPlanType()) {
                            case 1 -> "综合预案";
                            case 2 -> "专项预案";
                            case 3 -> "现场处置方案";
                            default -> "";
                        };
                    }
                    CellUtils.setStringVal(row, cellIndex++, planTypeText, style);
                    CellUtils.setStringVal(row, cellIndex, item.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("应急预案数据.xlsx", StandardCharsets.UTF_8));
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            log.error("导出应急预案失败", e);
        }
    }

    @Override
    public void importTemplate(HttpServletResponse response) {
        try (XSSFWorkbook workbook = ExcelUtils.getSheetAt("应急预案导入模板.xlsx")) {
            if (workbook == null) {
                return;
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("应急预案导入模板.xlsx", StandardCharsets.UTF_8));
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            log.error("导出应急预案导入模板失败", e);
        }
    }

    @Override
    @Log(title = "应急预案", businessType = BusinessType.INSERT)
    public AjaxResult importExcel(MultipartFile file) {
        if (file == null) {
            return AjaxResult.error("导入文件为空");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            return AjaxResult.error("导入文件名称为空");
        }
        int dotIndex = fileName.lastIndexOf(".");
        String fileType = dotIndex >= 0 ? fileName.substring(dotIndex) : "";
        if (!".xlsx".equalsIgnoreCase(fileType)) {
            return AjaxResult.error("不支持 " + fileType + " 文件类型");
        }

        List<String> entCodesLimit = null;
        if (GVarContainer.isNotAdmin()) {
            entCodesLimit = GVarContainer.getEntCodes();
            if (entCodesLimit == null || entCodesLimit.isEmpty()) {
                return AjaxResult.error("未分配企业权限，无法导入");
            }
        }

        List<EnterprisePart> enterprises = entCodesLimit == null
                ? enterpriseMapper.listAll()
                : enterpriseMapper.selectPartListAll(entCodesLimit);
        Map<String, String> entNameToCode = new HashMap<>();
        Set<String> entNameDuplicated = new HashSet<>();
        Set<String> entCodeSet = new HashSet<>();
        if (enterprises != null && !enterprises.isEmpty()) {
            for (EnterprisePart e : enterprises) {
                if (e == null) {
                    continue;
                }
                if (StringUtils.isNotBlank(e.getEntCode())) {
                    entCodeSet.add(e.getEntCode());
                }
                if (StringUtils.isBlank(e.getEntName()) || StringUtils.isBlank(e.getEntCode())) {
                    continue;
                }
                String prev = entNameToCode.putIfAbsent(e.getEntName(), e.getEntCode());
                if (prev != null && !prev.equals(e.getEntCode())) {
                    entNameDuplicated.add(e.getEntName());
                }
            }
        }

        List<String> errs = new ArrayList<>();
        List<EmergencyPlan> infos = new ArrayList<>();
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                int rowNo = i + 1;
                int index = 1;

                String entVal = CellUtils.getCellStringVal(row, index++);
                String entCode = resolveEntCode(entVal, entCodesLimit, entNameToCode, entNameDuplicated, entCodeSet, errs, rowNo);
                String planName = CellUtils.getCellStringVal(row, index++);
                if (StringUtils.isBlank(planName)) {
                    continue;
                }
                String version = CellUtils.getCellStringVal(row, index++);
                String riskUnit = CellUtils.getCellStringVal(row, index++);
                String handlePoints = CellUtils.getCellStringVal(row, index++);
                String planTypeText = CellUtils.getCellStringVal(row, index++);
                String remark = CellUtils.getCellStringVal(row, index);

                if (StringUtils.isBlank(entCode)) {
                    errs.add("第" + rowNo + "行所属企业为空/无法识别");
                    continue;
                }

                EmergencyPlan info = new EmergencyPlan();
                info.setPlanId(UlidCreator.getMonotonicUlid().toString());
                info.setEntCode(entCode);
                info.setPlanName(planName);
                info.setVersion(version);
                info.setRiskUnit(riskUnit);
                info.setHandlePoints(handlePoints);
                info.setPlanType(parsePlanTypeForImport(planTypeText));
                info.setRemark(remark);
                infos.add(info);
            }
        } catch (Exception e) {
            return AjaxResult.error("导入文件解析失败", e.getMessage());
        }

        if (!errs.isEmpty()) {
            return AjaxResult.error("文件内容异常，请检查", errs);
        }
        if (!infos.isEmpty()) {
            try {
                emergencyPlanMapper.batchInsert(infos);
            } catch (Exception e) {
                return AjaxResult.error("导入入库失败", e.getMessage());
            }
        }
        return AjaxResult.success(infos);
    }

    private Integer parsePlanTypeForImport(String planTypeText) {
        if (StringUtils.isBlank(planTypeText)) {
            return null;
        }
        String text = planTypeText.trim();
        if ("综合预案".equals(text)) {
            return 1;
        }
        if ("专项预案".equals(text)) {
            return 2;
        }
        if ("现场处置方案".equals(text)) {
            return 3;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveEntCode(String entVal,
                                 List<String> entCodesLimit,
                                 Map<String, String> entNameToCode,
                                 Set<String> entNameDuplicated,
                                 Set<String> entCodeSet,
                                 List<String> errs,
                                 int rowNo) {
        if (StringUtils.isBlank(entVal)) {
            if (entCodesLimit != null && entCodesLimit.size() == 1) {
                return entCodesLimit.getFirst();
            }
            return null;
        }
        String trimmed = entVal.trim();
        if (entCodeSet.contains(trimmed)) {
            return trimmed;
        }
        if (entNameDuplicated.contains(trimmed)) {
            errs.add("第" + rowNo + "行企业名称存在重名，请填写企业编码：" + trimmed);
            return null;
        }
        String code = entNameToCode.get(trimmed);
        if (StringUtils.isBlank(code)) {
            errs.add("第" + rowNo + "行所属企业不存在/无法识别：" + trimmed);
            return null;
        }
        return code;
    }
}


package com.zkhf.epmis.platform.envProtect.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.domain.IndustryCategory;
import com.zkhf.epmis.platform.base.service.IndustryCategoryService;
import com.zkhf.epmis.platform.enums.EnvManageGradeTypeEnum;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeProject;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeRelate;
import com.zkhf.epmis.platform.envProtect.domain.EnvMangeReq;
import com.zkhf.epmis.platform.envProtect.service.EnvMangeProjectService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.EnvMangeProjectMapper;
import com.zkhf.epmis.platform.mapper.envProtect.EnvMangeRelateMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业环评环保管理-项目Service业务层处理
 */
@Slf4j
@Service
public class EnvMangeProjectServiceImpl implements EnvMangeProjectService {

    private EnvMangeProjectMapper envMangeProjectMapper;

    @Autowired
    public void setEnvMangeProjectMapper(EnvMangeProjectMapper envMangeProjectMapper) {
        this.envMangeProjectMapper = envMangeProjectMapper;
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

    private IndustryCategoryService industryCategoryService;

    @Autowired
    public void setIndustryCategoryService(IndustryCategoryService industryCategoryService) {
        this.industryCategoryService = industryCategoryService;
    }

    @Override
    public AjaxResult projectRelateList(String projectId) {
        List<Map<String, Object>> list = envMangeRelateMapper.selectRelateByProjectId(projectId);
        List<Map<String, Object>> checkList = new ArrayList<>();
        List<Map<String, Object>> evaluateList = new ArrayList<>();
        for (Map<String, Object> e : list) {
            Integer relateType = MapUtil.getInt(e, "relateType");
            String relateId = MapUtil.getStr(e, "relateId");
            String relateName = MapUtil.getStr(e, "relateName");
            if (null == relateType || StringUtils.isEmpty(relateId) || StringUtils.isEmpty(relateName)) {
                continue;
            }
            if (EnvMangeRelate.RELATE_HP.equals(relateType)) {
                evaluateList.add(getRelate(relateId, relateName));
            } else if (EnvMangeRelate.RELATE_YS.equals(relateType)) {
                checkList.add(getRelate(relateId, relateName));
            }
        }
        AjaxResult result = AjaxResult.success();
        result.put("checkList", checkList);
        result.put("evaluateList", evaluateList);
        return result;
    }

    private Map<String, Object> getRelate(String relateId, String relateName) {
        Map<String, Object> map = new HashMap<>();
        map.put("relateId", relateId);
        map.put("relateName", relateName);
        return map;
    }

    @Override
    public AjaxResult selectMangeProjectList(EnvMangeReq req) {
        if (null == req) {
            req = new EnvMangeReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        boolean page = PageUtils.startPageCheckExists();
        List<EnvMangeProject> list = envMangeProjectMapper.selectMangeProjectList(req);
        // 国民经济行业类别设置
        fillInfo(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "企业环评环保管理-项目", businessType = BusinessType.EXPORT)
    public void exportMangeProject(EnvMangeReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvMangeReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环评环保管理.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvMangeProject> list = envMangeProjectMapper.selectMangeProjectList(req);
            if (null != list && !list.isEmpty()) {
                // 国民经济行业类别设置
                fillInfo(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvMangeProject project : list) {
                    row = sheet.createRow(rowIndex++);
                    // 设置内容
                    setEnvMangeProjectCellValue(row, index++, style, project);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业环评环保管理.xlsx", StandardCharsets.UTF_8));
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

    private void fillInfo(List<EnvMangeProject> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 国民经济行业类别设置
        Map<String, IndustryCategory> inMap = industryCategoryService.selectIndustryCategoryMap();
        list.forEach(e -> {
            e.setIndustryCategoryList(new ArrayList<>());
            e.setIndustryCodeList(new ArrayList<>());
            e.setIndustryList(new ArrayList<>());
            industryCategoryService.industrySet(inMap, e.getIndustryCategoryList(), e.getIndustryCodeList(), e.getIndustryList(), e.getIndustryCategory());
        });
    }

    /**
     * 设置导出文件的项目内容
     */
    private void setEnvMangeProjectCellValue(Row row, int index, CellStyle style, EnvMangeProject project) {
        int cellIndex = 0;
        // 序号
        Cell cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(index);
        // 企业名称
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getEntName());
        // 项目名称
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getProjectName());
        // 项目代码
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getProjectCode());
        // 项目性质
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getProjectNature());
        // 主要建设内容
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getMainContent());
        // 产品
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getProduct());
        // 产能
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getPCapacity());
        // 生产班制
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getProductShiftSys());
        // 建设地点
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getConstructSide());
        // 国民经济行业类别
        cell = CellUtils.getCell(row, cellIndex++, style);
        if (null != project.getIndustryCategoryList() && !project.getIndustryCategoryList().isEmpty()) {
            cell.setCellValue(String.join(", ", project.getIndustryCategoryList()));
        }
        // 行业代码
        cell = CellUtils.getCell(row, cellIndex++, style);
        if (null != project.getIndustryCodeList() && !project.getIndustryCodeList().isEmpty()) {
            cell.setCellValue(String.join(", ", project.getIndustryCodeList()));
        }
        // 环评等级
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(EnvManageGradeTypeEnum.getNameByCode(project.getGrade()));
        // 判断依据
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getJudgmentReason());
        // 用地面积(平米)
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(null == project.getLandArea() ? "" : Double.toString(project.getLandArea()));
        // 对外立项时间
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(null == project.getExtApprTime() ? "" : project.getExtApprTime().toString());
        // 开工时间
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(null == project.getCommenceTime() ? "" : project.getCommenceTime().toString());
        // 投产时间
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(null == project.getProductTime() ? "" : project.getProductTime().toString());
        // 主要环保设施
        cell = CellUtils.getCell(row, cellIndex++, style);
        cell.setCellValue(project.getMainEnvFacilities());
        // 备注
        cell = CellUtils.getCell(row, cellIndex, style);
        cell.setCellValue(project.getRemark());
    }

    @Override
    @Log(title = "企业环评环保管理-项目", businessType = BusinessType.INSERT)
    public AjaxResult insertMangeProject(EnvMangeProject info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setProjectId(UlidCreator.getMonotonicUlid().toString());
        int count = envMangeProjectMapper.insertMangeProject(info);
        if (count > 0) {
            // 修改关联关系
            updateRelate(info);
            // 更新附件
            if (null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getProjectId(), AnnexTypeEnum.entEnvMangeProject.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业环评环保管理-项目", businessType = BusinessType.UPDATE)
    public AjaxResult updateMangeProject(EnvMangeProject info) {
        int count = envMangeProjectMapper.updateMangeProject(info);
        if (count > 0) {
            // 修改关联关系
            updateRelate(info);
            // 更新附件
            annexService.updateAnnex(info.getProjectId(), AnnexTypeEnum.entEnvMangeProject.name(), info.getAnnexIds());
        }
        return AjaxResult.success();
    }

    private void updateRelate(EnvMangeProject info) {
        // 删除关联关系
        envMangeRelateMapper.deleteRelate(info.getProjectId(), null, null);
        // 添加新的关联关系
        List<EnvMangeRelate> relateList = new ArrayList<>();
        if (null != info.getEvaluateIdList() && !info.getEvaluateIdList().isEmpty()) {
            info.getEvaluateIdList().forEach( e -> relateList.add(EnvMangeRelate.builder()
                    .projectId(info.getProjectId())
                    .relateType(EnvMangeRelate.RELATE_HP)
                    .relateId(e)
                    .build()));
        }
        if (null != info.getCheckIdList() && !info.getCheckIdList().isEmpty()) {
            info.getCheckIdList().forEach( e -> relateList.add(EnvMangeRelate.builder()
                    .projectId(info.getProjectId())
                    .relateType(EnvMangeRelate.RELATE_YS)
                    .relateId(e)
                    .build()));
        }
        if (!relateList.isEmpty()) {
            // 添加新的
            envMangeRelateMapper.batchInsertRelate(relateList);
        }
    }

    @Override
    @Log(title = "企业环评环保管理-项目", businessType = BusinessType.DELETE)
    public AjaxResult deleteMangeProjectById(String id) {
        if (StringUtils.isEmpty(id)) {
            return AjaxResult.error("请求信息为空");
        }
        int count = envMangeProjectMapper.deleteMangeProjectById(id);
        if (count > 0) {
            // 删除关联关系
            envMangeRelateMapper.deleteRelate(id, null, null);
            // 删除项目附件
            annexService.updateAnnex(id, AnnexTypeEnum.entEnvMangeProject.name(), null);
        }
        return AjaxResult.success(count);
    }
}


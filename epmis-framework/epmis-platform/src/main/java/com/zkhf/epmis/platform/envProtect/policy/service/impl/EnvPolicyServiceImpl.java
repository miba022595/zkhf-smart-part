package com.zkhf.epmis.platform.envProtect.policy.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.domain.Districts;
import com.zkhf.epmis.platform.base.domain.IndustryCategory;
import com.zkhf.epmis.platform.base.service.DictService;
import com.zkhf.epmis.platform.base.service.DistrictsService;
import com.zkhf.epmis.platform.base.service.IndustryCategoryService;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyInfo;
import com.zkhf.epmis.platform.envProtect.policy.domain.EnvPolicyReq;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvLearnService;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvPolicyService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.policy.EnvPolicyMapper;
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
import java.util.*;

/**
 * 环境政策法规信息-Service业务层处理
 */
@Slf4j
@Service
public class EnvPolicyServiceImpl implements EnvPolicyService {

    private EnvPolicyMapper envPolicyMapper;
    @Autowired
    public void setEnvPolicyMapper(EnvPolicyMapper envPolicyMapper) {
        this.envPolicyMapper = envPolicyMapper;
    }

    private DictService dictService;
    @Autowired
    public void setDictService(DictService dictService) {
        this.dictService = dictService;
    }

    private IndustryCategoryService industryCategoryService;
    @Autowired
    public void setIndustryCategoryService(IndustryCategoryService industryCategoryService) {
        this.industryCategoryService = industryCategoryService;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private EnvLearnService envLearnService;
    @Autowired
    public void setEnvLearnService(EnvLearnService envLearnService) {
        this.envLearnService = envLearnService;
    }

    private DistrictsService districtsService;
    @Autowired
    public void setDistrictsService(DistrictsService districtsService) {
        this.districtsService = districtsService;
    }

    @Override
    public AjaxResult envPolicyListAll() {
        // 添加权限
        List<?> list;
        if (GVarContainer.isAdmin()) {
            list = envPolicyMapper.getAllEnvPolicy(null);
        } else {
            list = envPolicyMapper.getAllEnvPolicy(GVarContainer.getEntCodes());
        }
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult selectEnvPolicyList(EnvPolicyReq req) {
        if (null == req) {
            req = new EnvPolicyReq();
        }
        // 添加权限，没权限且不查公共的直接返回空列表
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (req.getEntCodes().isEmpty() && !pubSign.equals(req.getPubSign())) {
                return AjaxResult.success(new ArrayList<>());
            }
        }
        PageUtils.startPage();
        List<EnvPolicyInfo> list = envPolicyMapper.selectEnvPolicyList(req);
        // 填充内容
        fillData(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "环境政策法规列表", businessType = BusinessType.EXPORT)
    public void exportEnvPolicy(EnvPolicyReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvPolicyReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环境政策法规模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限，没权限且不查公共的直接赋值为空列表
            List<EnvPolicyInfo> list;
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
                if (req.getEntCodes().isEmpty() && !pubSign.equals(req.getPubSign())) {
                    list = new ArrayList<>();
                } else {
                    list = envPolicyMapper.selectEnvPolicyList(req);
                }
            } else {
                list = envPolicyMapper.selectEnvPolicyList(req);
            }
            if (null != list && !list.isEmpty()) {
                // 填充内容
                fillData(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2; // 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvPolicyInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 关联企业
                    CellUtils.setStringVal(row, cellIndex++, info.getEntName(), style);
                    // 法规标准名称
                    CellUtils.setStringVal(row, cellIndex++, info.getPolicyName(), style);
                    // 政策法规编号（文号）
                    CellUtils.setStringVal(row, cellIndex++, info.getPolicyCode(), style);
                    // 发行部门
                    CellUtils.setStringVal(row, cellIndex++, info.getIssueDept(), style);
                    // 发布地区（国家/地方）
                    CellUtils.setStringVal(row, cellIndex++, info.getRegionDesc(), style);
                    // 法规类型
                    CellUtils.setStringVal(row, cellIndex++, info.getPolicyTypeDesc(), style);
                    // 行业类别
                    String industryCategoryText = null;
                    if (null != info.getIndustryCategoryList() && !info.getIndustryCategoryList().isEmpty()) {
                        industryCategoryText = String.join(", ", info.getIndustryCategoryList());
                    }
                    CellUtils.setStringVal(row, cellIndex++, industryCategoryText, style);
                    // 环保要素
                    CellUtils.setStringVal(row, cellIndex++, info.getEnvElementDesc(), style);
                    // 管理要素
                    CellUtils.setStringVal(row, cellIndex++, info.getMangeElementDesc(), style);
                    // 重要程度
                    CellUtils.setStringVal(row, cellIndex++, info.getSignificanceDesc(), style);
                    // 发布日期
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getPublishDate(), DateUtils.yy_m_d, style);
                    // 实施日期
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getImplementDate(), DateUtils.yy_m_d, style);
                    // 状态：0-作废，1-有效，2-修订
                    CellUtils.setStringVal(row, cellIndex++, info.getStatusDesc(), style);
                    // 适用范围
                    CellUtils.setStringVal(row, cellIndex++, info.getApplicableScope(), style);
                    // 主要内容
                    CellUtils.setStringVal(row, cellIndex, info.getMainContent(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环境政策法规列表.xlsx", StandardCharsets.UTF_8));
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

    private void fillData(List<EnvPolicyInfo> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 转换地区信息
        List<Districts> districts = districtsService.selectDistrictsList();
        Map<String, String> districtMap = new HashMap<>();
        districts.forEach( e -> districtMap.put(e.getId() + "", e.getExtName()));
        /*
        policyType: 法规类型, 字典 regulatory_type
        envElement: 环保要素，字典 env_element（多选）
        mangeElement: 管理要素，字典 mange_element（多选）
        significance: 重要程度，字典 sys_significance
        status: 法规状态，字典regulatory_status
        */
        List<String> dictTypes = Arrays.asList("regulatory_type", "env_element", "mange_element", "sys_significance", "regulatory_status");
        Map<String, Map<String, String>> dictMap = dictService.getDataMapByTypes(dictTypes);
        // 国民经济行业类别设置
        Map<String, IndustryCategory> inMap = industryCategoryService.selectIndustryCategoryMap();
        list.forEach( e -> {
            if (pubSign.equals(e.getEntCode())) {
                e.setEntName("公共法规文件");
            }
            Map<String, String> sub = dictMap.get("regulatory_type");
            if (null != sub && sub.containsKey(e.getPolicyType())) {
                e.setPolicyTypeDesc(sub.get(e.getPolicyType()));
            }
            sub = dictMap.get("env_element");
            if (null != sub && StringUtils.isNotEmpty(e.getEnvElement())) {
                String item;
                for (String k : e.getEnvElement().split(",")) {
                    item = sub.get(k);
                    if (StringUtils.isEmpty(item)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(e.getEnvElementDesc())) {
                        e.setEnvElementDesc(item);
                    } else {
                        e.setEnvElementDesc(e.getEnvElementDesc() + "," + item);
                    }
                }
            }
            sub = dictMap.get("mange_element");
            if (null != sub && StringUtils.isNotEmpty(e.getMangeElement())) {
                String item;
                for (String k : e.getMangeElement().split(",")) {
                    item = sub.get(k);
                    if (StringUtils.isEmpty(item)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(e.getMangeElementDesc())) {
                        e.setMangeElementDesc(item);
                    } else {
                        e.setMangeElementDesc(e.getMangeElementDesc() + "," + item);
                    }
                }
            }
            sub = dictMap.get("sys_significance");
            if (null != sub && sub.containsKey(e.getSignificance())) {
                e.setSignificanceDesc(sub.get(e.getSignificance()));
            }
            sub = dictMap.get("regulatory_status");
            String status = e.getStatus() + "";
            if (null != sub && sub.containsKey(status)) {
                e.setStatusDesc(sub.get(status));
            }
            // 国民经济行业类别设置
            e.setIndustryCategoryList(new ArrayList<>());
            e.setIndustryCodeList(new ArrayList<>());
            e.setIndustryList(new ArrayList<>());
            industryCategoryService.industrySet(inMap, e.getIndustryCategoryList(), e.getIndustryCodeList(), e.getIndustryList(), e.getIndustryCategory());
            if (StringUtils.isNotEmpty(e.getRegion())) {
                StringBuilder bu;
                if ("-1".equals(e.getRegion())) {
                    bu = new StringBuilder("全国");
                } else {
                    bu = new StringBuilder();
                    for (String s : e.getRegion().split(",")) {
                        if (districtMap.containsKey(s)) {
                            bu.append(districtMap.get(s));
                        }
                    }
                }
                e.setRegionDesc(bu.toString());
            }
        });
    }

    @Override
    @Log(title = "环境政策法规列表", businessType = BusinessType.INSERT)
    public AjaxResult insertEnvPolicy(EnvPolicyInfo info) {
        if (GVarContainer.isNotAdmin()) {
            if (StringUtils.isEmpty(info.getEntCode())) {
                return AjaxResult.error("未指定所属企业");
            }
        } else {
            // admin时，未设置所属企业则为公共的法规
            if (StringUtils.isEmpty(info.getEntCode())) {
                info.setEntCode(pubSign);
            }
        }
        info.setPolicyId(UlidCreator.getMonotonicUlid().toString());
        int count = envPolicyMapper.insertEnvPolicy(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(info.getPolicyId(), AnnexTypeEnum.envPolicy.name(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环境政策法规列表", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvPolicy(EnvPolicyInfo info) {
        // 非admin不能修改公共法规
        if (GVarContainer.isNotAdmin()) {
            EnvPolicyInfo old = envPolicyMapper.selectEnvPolicyById(info.getPolicyId());
            if (null != old && pubSign.equals(old.getEntCode())) {
                return AjaxResult.error("无权限修改");
            }
        }
        info.setUpdateTime(LocalDateTime.now());
        int count = envPolicyMapper.updateEnvPolicy(info);
        if (count > 0) {
            annexService.updateAnnex(info.getPolicyId(), AnnexTypeEnum.envPolicy.name(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环境政策法规列表", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnvPolicyById(String policyId) {
        // 判断是否关联学习
        int count = envLearnService.checkExistsLearnByPolicyId(policyId);
        if (count > 0) {
            return AjaxResult.error("该文件已关联学习，不允许删除");
        }
        // 非admin不能删除公共法规
        if (GVarContainer.isNotAdmin()) {
            EnvPolicyInfo old = envPolicyMapper.selectEnvPolicyById(policyId);
            if (null != old && pubSign.equals(old.getEntCode())) {
                return AjaxResult.error("无权限删除");
            }
        }
        count = envPolicyMapper.deleteEnvPolicyById(policyId);
        if (count > 0) {
            // 删除附件
            annexService.updateAnnex(policyId, AnnexTypeEnum.envPolicy.name(), null);
        }
        return AjaxResult.success(count);
    }
}


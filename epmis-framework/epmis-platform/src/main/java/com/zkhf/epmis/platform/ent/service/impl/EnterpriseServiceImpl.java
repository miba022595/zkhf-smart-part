package com.zkhf.epmis.platform.ent.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.domain.Districts;
import com.zkhf.epmis.platform.base.service.DictService;
import com.zkhf.epmis.platform.base.service.DistrictsService;
import com.zkhf.epmis.platform.ent.domain.*;
import com.zkhf.epmis.platform.ent.service.EnterpriseService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.EnterpriseMapper;
import com.zkhf.epmis.platform.task.spider.handler.LicenseSpiderJob;
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
import java.time.LocalDateTime;
import java.util.*;

/**
 * 企业基础Service业务层处理
 */
@Slf4j
@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    private EnterpriseMapper enterpriseMapper;
    @Autowired
    public void setEnterpriseMapper(EnterpriseMapper enterpriseMapper) {
        this.enterpriseMapper = enterpriseMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private DictService dictService;
    @Autowired
    public void setDictService(DictService dictService) {
        this.dictService = dictService;
    }

    private DistrictsService districtsService;
    @Autowired
    public void setDistrictsService(DistrictsService districtsService) {
        this.districtsService = districtsService;
    }

    private LicenseSpiderJob licenseSpiderJob;
    @Autowired
    public void setLicenseSpiderJob(LicenseSpiderJob licenseSpiderJob) {
        this.licenseSpiderJob = licenseSpiderJob;
    }

    @Override
    public List<EnterprisePart> listAll() {
        return enterpriseMapper.listAll();
    }

    @Override
    public AjaxResult listByPollType(Integer pollType) {
        if (OutPutTypeEnum.notContainsType(pollType)) {
            return AjaxResult.error("未知的排口类型");
        }
        AjaxResult result = AjaxResult.success();
        // 添加权限
        List<EnterprisePart> list;
        if (GVarContainer.isAdmin()) {
            list = enterpriseMapper.selectPartListByPollType(null, pollType);
        } else {
            list = enterpriseMapper.selectPartListByPollType(GVarContainer.getEntCodes(), pollType);
        }
        if (null != list) {
            result.put("total", list.size());
        }
        result.put("data", list);
        return result;
    }

    @Override
    public AjaxResult listByRegion(String region) {
        AjaxResult result = AjaxResult.success();
        // 添加权限
        List<EnterprisePart> list;
        if (GVarContainer.isAdmin()) {
            list = enterpriseMapper.selectPartListByRegion(null, region);
        } else {
            list = enterpriseMapper.selectPartListByRegion(GVarContainer.getEntCodes(), region);
        }
        if (null != list) {
            result.put("total", list.size());
        }
        // 将企业加到子节点上
        result.put("data", buildRegionTree(list));
        return result;
    }

    @Override
    public AjaxResult selectPartListAll() {
        AjaxResult result = AjaxResult.success();
        // 添加权限
        List<EnterprisePart> list;
        if (GVarContainer.isAdmin()) {
            list = enterpriseMapper.selectPartListAll(null);
        } else {
            list = enterpriseMapper.selectPartListAll(GVarContainer.getEntCodes());
        }
        if (null != list) {
            result.put("total", list.size());
        }
        result.put("data", list);
        return result;
    }

    @Override
    public AjaxResult outPutTree(JSONObject req) {
        if (null == req) {
            req = new JSONObject();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.put("entCodes", GVarContainer.getEntCodes());
        }
        List<Map<String, Object>> list = enterpriseMapper.outPutTree(req);
        if (null != list && !list.isEmpty()) {
            // 补全字段
            list.forEach( e -> {
                e.put("entCode", e.get("entCode"));
                e.put("parentCode", e.get("parentCode"));
                e.put("entName", e.get("entName"));
                Object obj = e.get("outPutList");
                if (obj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> sub = (List<Map<String, Object>>) obj;
                    sub.forEach( f -> {
                        f.put("outPutId", f.get("outPutId"));
                        f.put("outPutCode", f.get("outPutCode"));
                        f.put("outPutName", f.get("outPutName"));
                        f.put("outPutType", f.get("outPutType"));
                        f.put("longitude", f.get("longitude"));
                        f.put("latitude", f.get("latitude"));
                        f.put("clockRange", f.get("clockRange"));
                    });
                } else {
                    e.put("outPutList", new ArrayList<>());
                }
            });
        }
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult outPutTreeByEnt(Integer pollType) {
        AjaxResult result = AjaxResult.success();
        // 添加权限
        List<EnterprisePart> list;
        if (GVarContainer.isAdmin()) {
            list = enterpriseMapper.outPutTreeByEnt(null, pollType);
        } else {
            list = enterpriseMapper.outPutTreeByEnt(GVarContainer.getEntCodes(), pollType);
        }
        if (null != list) {
            result.put("total", list.size());
        }
        result.put("data", buildEnterpriseTree(list));
        return result;
    }

    private List<EntTree> buildEnterpriseTree(List<EnterprisePart> list) {
        if (null == list || list.isEmpty()) {
            return new ArrayList<>();
        }
        // 1. 使用Map存储所有企业，key为entCode
        Map<String, EntTree> treeMap = new HashMap<>();
        List<EntTree> roots = new ArrayList<>();

        // 第一次遍历：放入Map
        for (EnterprisePart e : list) {
            treeMap.put(e.getEntCode(), EntTree.builder()
                    .entCode(e.getEntCode())
                    .parentCode(e.getParentCode())
                    .entName(e.getEntName())
                    .socialCreditCode(e.getSocialCreditCode())
                    .shorterName(e.getShorterName())
                    .longitude(e.getLongitude())
                    .latitude(e.getLatitude())
                    .subList(new ArrayList<>())
                    .build());
        }

        // 第二次遍历：建立父子关系
        for (EnterprisePart e : list) {
            String parentCode = e.getParentCode();
            // 找到父节点，添加到父节点的children中
            EntTree parent = treeMap.get(parentCode);
            if (parent != null) {
                parent.getSubList().add(treeMap.get(e.getEntCode()));
            } else {
                // 父节点不存在，作为根节点处理
                EntTree node = treeMap.get(e.getEntCode());
                roots.add(node);
            }
        }
        // 一次性构建所有subList
        for (EnterprisePart node : list) {
            if (null == node.getOutList() || node.getOutList().isEmpty()) {
                continue;
            }
            if (!treeMap.containsKey(node.getEntCode())) {
                continue;
            }
            treeMap.get(node.getEntCode()).getSubList().addAll(node.getOutList());
        }
        return roots;
    }

    @Override
    public AjaxResult outPutTreeByRegion() {
        AjaxResult result = AjaxResult.success();
        // 添加权限
        List<EnterprisePart> list;
        if (GVarContainer.isAdmin()) {
            list = enterpriseMapper.outPutTreeByRegion(null);
        } else {
            List<String> entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty()) {
                list = null;
            } else {
                list = enterpriseMapper.outPutTreeByRegion(entCodes);
            }
        }
        if (null != list) {
            result.put("total", list.size());
        }
        // 将企业加到子节点上
        result.put("data", buildRegionTree(list));
        return result;
    }

    private List<RegionEnt> buildRegionTree(List<EnterprisePart> enterprises) {
        if (enterprises == null || enterprises.isEmpty()) {
            return new ArrayList<>();
        }
        // 转换地区信息
        List<Districts> districts = districtsService.selectDistrictsList();
        Map<String, String> districtMap = new HashMap<>();
        districts.forEach( e -> districtMap.put(e.getId() + "", e.getExtName()));
        // 1. 构建所有节点
        List<RegionEnt> roots = new ArrayList<>();
        Map<String, RegionEnt> nodeMap = new HashMap<>();

        Map<String, List<EnterprisePart>> entMap = new HashMap<>();

        for (EnterprisePart ent : enterprises) {
            if (StringUtils.isEmpty(ent.getRegion())) {
                continue;
            }
            String[] regionIds = ent.getRegion().split(",");

            // 创建每一层节点并添加企业
            for (int i = 0; i < regionIds.length; i++) {
                String current = regionIds[i];
                RegionEnt cr;
                if (!nodeMap.containsKey(current)) {
                    cr = new RegionEnt();
                    cr.setRegion(current);
                    cr.setRegionDesc(districtMap.get(current));
                    cr.setSubList(new ArrayList<>());
                    nodeMap.put(current, cr);
                    if (i == 0) {
                        roots.add(cr);
                    }
                    if (i > 0) {
                        nodeMap.get(regionIds[i - 1]).getSubList().add(cr);
                    }
                }
                if (i == regionIds.length - 1) {
                    if (!entMap.containsKey(current)) {
                        entMap.put(current, new ArrayList<>());
                    }
                    entMap.get(current).add(ent);
                }
            }
        }
        entMap.forEach( (k, v) -> {
            if (nodeMap.containsKey(k)) {
                nodeMap.get(k).getSubList().addAll(v);
            }
        });
        // 2. 返回根节点
        return roots;
    }

    @Override
    public AjaxResult selectList(EnterpriseReq req) {
        if (null == req) {
            req = new EnterpriseReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<Enterprise> list = enterpriseMapper.selectList(req);
        // 填充内容
        fill(list);
        AjaxResult result = PageUtils.getAjaxResult(list, page);
        if (null == list || list.isEmpty()) {
            return result;
        }
        Map<String, Enterprise> entMap = new HashMap<>();
        list.forEach( e -> {
            e.setAnnexList(new ArrayList<>());
            entMap.put(e.getEntCode(), e);
        });
        AnnexReq annexReq = new AnnexReq();
        annexReq.setSourceType(AnnexTypeEnum.enterprise.name());
        // 添加厂区分布图
        List<AnnexInfo> annexList = annexService.selectAnnexList(annexReq);
        if (null == annexList || annexList.isEmpty()) {
            return result;
        }
        annexList.forEach( e -> {
            if (entMap.containsKey(e.getSourceId())) {
                entMap.get(e.getSourceId()).getAnnexList().add(e);
            }
        });
        return result;
    }

    @Override
    @Log(title = "企业基础列表", businessType = BusinessType.EXPORT)
    public void exportList(EnterpriseReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnterpriseReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<Enterprise> list = enterpriseMapper.selectList(req);
            if (null != list && !list.isEmpty()) {
                // 填充内容
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 1;// 从第2行开始插入
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (Enterprise ent : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntName());
                    // 社会统一信用代码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getSocialCreditCode());
                    // 企业简称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getShorterName());
                    // 法定代表人
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getLegalPerson());
                    // 所在地区
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getRegionDesc());
                    // 详细地址
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getAddress());
                    // 企业状态
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntStatusDesc());
                    // 企业规模
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntScaleDesc());
                    // 企业类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntTypeDesc());
                    // 行业类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getIndustryTypeDesc());
                    // 污染源类别
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getPollutionClassDesc());
                    // 企业负责人姓名
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntDirectorName());
                    // 企业负责人电话
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntDirectorPhone());
                    // 企业负责人邮箱
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEntDirectorEmail());
                    // 环保负责人姓名
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEnvDirectorName());
                    // 环保负责人电话
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEnvDirectorPhone());
                    // 环保负责人邮箱
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEnvDirectorEmail());
                    // 环保制度名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEnvPolicyName());
                    // 环保制度执行日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != ent.getEnvPolicyDate()) {
                        cell.setCellValue(ent.getEnvPolicyDate().format(DateUtils.yy_m_d));
                    }
                    // 环保制度执行级别
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(ent.getEnvPolicyLevel());
                    // 企业介绍
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(ent.getEntIntroduction());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<Enterprise> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 转换地区信息
        List<Districts> districts = districtsService.selectDistrictsList();
        Map<Long, String> districtMap = new HashMap<>();
        districts.forEach( e -> districtMap.put(e.getId(), e.getExtName()));
        list.forEach(e -> {
            if (StringUtils.isNotEmpty(e.getRegion())) {
                StringBuilder bu = new StringBuilder();
                for (String s : e.getRegion().split(",")) {
                    Long id = StringUtils.strToLong(s, null);
                    if (districtMap.containsKey(id)) {
                        bu.append(districtMap.get(id));
                    }
                }
                e.setRegionDesc(bu.toString());
            }
        });
        /*
        entStatus: 企业状态, 字典enterprise_status
        entScale: 企业规模，字典enterprise_scale
        entType: 企业类型，字典enterprise_type
        industryType: 行业类型，字典industry_type
        pollutionClass: 污染源类别，字典types_of_pollution_sources
        */
        List<String> dictTypes = Arrays.asList("enterprise_status", "enterprise_scale", "enterprise_type", "industry_type", "types_of_pollution_sources");
        Map<String, Map<String, String>> dictMap = dictService.getDataMapByTypes(dictTypes);
        list.forEach( e -> {
            Map<String, String> sub = dictMap.get("enterprise_status");
            if (null != sub && sub.containsKey(e.getEntStatus())) {
                e.setEntStatusDesc(sub.get(e.getEntStatus()));
            }
            sub = dictMap.get("enterprise_scale");
            if (null != sub && sub.containsKey(e.getEntScale())) {
                e.setEntScaleDesc(sub.get(e.getEntScale()));
            }
            sub = dictMap.get("enterprise_type");
            if (null != sub && sub.containsKey(e.getEntType())) {
                e.setEntTypeDesc(sub.get(e.getEntType()));
            }
            sub = dictMap.get("industry_type");
            if (null != sub && sub.containsKey(e.getIndustryType())) {
                e.setIndustryTypeDesc(sub.get(e.getIndustryType()));
            }
            sub = dictMap.get("types_of_pollution_sources");
            if (null != sub && StringUtils.isNotEmpty(e.getPollutionClass())) {
                String item;
                for (String k : e.getPollutionClass().split(",")) {
                    item = sub.get(k);
                    if (StringUtils.isEmpty(item)) {
                        continue;
                    }
                    if (StringUtils.isEmpty(e.getPollutionClassDesc())) {
                        e.setPollutionClassDesc(item);
                    } else {
                        e.setPollutionClassDesc(e.getPollutionClassDesc() + "," + item);
                    }
                }
            }
        });
    }

    @Override
    @Log(title = "企业基础列表", businessType = BusinessType.INSERT)
    public AjaxResult insertEnterprise(Enterprise info) {
        info.setEntCode(UlidCreator.getMonotonicUlid().toString());
        info.setCreateTime(LocalDateTime.now());
        int result = enterpriseMapper.insertEnterprise(info);
        if (result > 0) {
            // 设置附件
            if (info.getAnnexIds() != null && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getEntCode(), AnnexTypeEnum.enterprise.name(), info.getAnnexIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业基础列表", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnterprise(Enterprise info) {
        info.setUpdateTime(LocalDateTime.now());
        int result = enterpriseMapper.updateEnterprise(info);
        // 修改附件信息
        if (result > 0) {
            annexService.updateAnnex(info.getEntCode(), AnnexTypeEnum.enterprise.name(), info.getAnnexIds());
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业基础列表", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnterpriseByEntCode(String entCode) {
        // 判断企业是否包含下级企业，包含则不能删除
        List<Enterprise> parentList = enterpriseMapper.selectListByParentCode(entCode);
        if (null != parentList && !parentList.isEmpty()) {
            return AjaxResult.error("当前企业包含子级，不能删除");
        }
        int result = enterpriseMapper.deleteEnterpriseById(entCode);
        if (result > 0) {
            // 删除企业关联排口关注
            enterpriseMapper.deleteEntOutputAtt(entCode);
            // 删除企业关联排口的污染物
            enterpriseMapper.deleteEntOutputPoll(entCode);
            // 删除企业关联排口
            enterpriseMapper.deleteEntOutput(entCode);
            // 删除企业关联的用户
            enterpriseMapper.deleteUserEnt(entCode);
            // 删除附件
            annexService.updateAnnex(entCode, AnnexTypeEnum.enterprise.name(), null);
        }
        return AjaxResult.success();
    }

    @Override
    public List<EnterprisePart> selectPartListAllByInternal(String entCode) {
        List<String> codeList = null;
        if (StringUtils.isEmpty(entCode)) {
            if (GVarContainer.isNotAdmin()) {
                codeList = GVarContainer.getEntCodes();
            }
        } else {
            codeList = Collections.singletonList(entCode);
        }
        return enterpriseMapper.selectPartListAll(codeList);
    }

    @Override
    public AjaxResult entLicenseSpider(String entCode) {
        try {
            if (StringUtils.isEmpty(entCode)) {
                return AjaxResult.error("查询参数为空");
            }
            List<String> codeList = Collections.singletonList(entCode);
            List<EnterprisePart> entList = enterpriseMapper.selectPartListAll(codeList);
            if (null == entList || entList.isEmpty()) {
                return AjaxResult.error("未知的企业编码");
            }
            licenseSpiderJob.licenseSpiderSingle(entList.getFirst());
            return AjaxResult.success("许可信息自动获取成功");
        } catch (Exception e) {
            log.error("许可信息自动获取失败 {}", entCode, e);
        }
        return AjaxResult.error("许可信息自动获取失败，稍后重试");
    }
}


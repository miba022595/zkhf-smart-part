package com.zkhf.epmis.process.solidWaste.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.EntInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.solidWaste.WasteTotalPlanMapper;
import com.zkhf.epmis.process.solidWaste.domain.WasteDict;
import com.zkhf.epmis.process.solidWaste.domain.WasteLibReq;
import com.zkhf.epmis.process.solidWaste.domain.WasteTotalPlan;
import com.zkhf.epmis.process.solidWaste.service.WasteDictService;
import com.zkhf.epmis.process.solidWaste.service.WasteTotalPlanService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
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
 * 固废总量控制计划Service业务层处理
 */
@Slf4j
@Service
public class WasteTotalPlanServiceImpl implements WasteTotalPlanService {

    private WasteTotalPlanMapper wasteTotalPlanMapper;
    @Autowired
    public void setWasteTotalPlanMapper(WasteTotalPlanMapper wasteTotalPlanMapper) {
        this.wasteTotalPlanMapper = wasteTotalPlanMapper;
    }

    private WasteDictService wasteDictService;
    @Autowired
    public void setWasteDictService(WasteDictService wasteDictService) {
        this.wasteDictService = wasteDictService;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectWasteTotalPlanList(WasteLibReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 固废种类管理列表查询
        boolean page = PageUtils.startPageCheckExists();
        List<WasteTotalPlan> list = wasteTotalPlanMapper.selectWasteTotalPlanList(req);
        // 填充信息
        fill(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "固废总量控制计划", businessType = BusinessType.EXPORT)
    public void exportWasteTotalPlan(WasteLibReq req, HttpServletResponse response) {
        // 请求参数转换
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("固废总量控制列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<WasteTotalPlan> list = null;
            if (null != req) {
                list = wasteTotalPlanMapper.selectWasteTotalPlanList(req);
            }
            if (null != list && !list.isEmpty()) {
                // 填充内容
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3; // 从第4行开始插入
                // 设置单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN3 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,3);

                List<CellRangeAddress> mergeRegions = new ArrayList<>();

                // ========== 第1层分组：企业（按 entCode）==========
                int group1StartRow = rowIndex;
                int group1Size = 0;
                String prevEntCode = "";

                // ========== 第2层分组：排放口（按 outPutId）==========
                int group2StartRow = rowIndex;
                int group2Size = 0;
                String prevOutPutId = "";

                // ========== 第3层分组：固废分类（按 wasteCategory）==========
                int group3StartRow = rowIndex;
                int group3Size = 0;
                String prevWasteCategory = "";

                // ========== 第4层分组：固废类别（按 wasteType）==========
                int group4StartRow = rowIndex;
                int group4Size = 0;
                String prevWasteType = "";
                Row row;
                int serialNo = 1; // 序号计数器（按第2层分组：排放口递增）
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (int i = 0; i < list.size(); i++) {
                    row = sheet.createRow(rowIndex++);
                    WasteTotalPlan plan = list.get(i);

                    int cellIndex = 0;

                    // 获取当前行的分组字段值（null转空字符串）
                    String curEntCode = plan.getEntCode() == null ? "" : plan.getEntCode();
                    String curOutPutId = plan.getOutPutId() == null ? "" : plan.getOutPutId();
                    String curWasteCategory = plan.getWasteCategory() == null ? "" : plan.getWasteCategory();
                    String curWasteType = plan.getWasteType() == null ? "" : plan.getWasteType();

                    // 判断各层是否是新组的开始（基于上层变化或自身变化）
                    boolean isGroup1Start = !curEntCode.equals(prevEntCode);              // 企业组开始
                    boolean isGroup2Start = isGroup1Start || !curOutPutId.equals(prevOutPutId); // 排放口组开始
                    boolean isGroup3Start = isGroup2Start || !curWasteCategory.equals(prevWasteCategory); // 固废分类组开始
                    boolean isGroup4Start = isGroup3Start || !curWasteType.equals(prevWasteType); // 固废类别组开始

                    // 序号列
                    Cell cell = CellUtils.getCell(row, cellIndex++, styleN0);
                    if (isGroup2Start) {
                        cell.setCellValue(serialNo);
                    }

                    // 企业名称列
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroup1Start && StringUtils.isNotEmpty(plan.getEntName())) {
                        cell.setCellValue(plan.getEntName());
                    }

                    // 归属排口code
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroup2Start && StringUtils.isNotEmpty(plan.getOutPutCode())) {
                        cell.setCellValue(plan.getOutPutCode());
                    }

                    // 归属排口name
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroup2Start && StringUtils.isNotEmpty(plan.getOutPutName())) {
                        cell.setCellValue(plan.getOutPutName());
                    }

                    // 固废分类列
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroup3Start && StringUtils.isNotEmpty(plan.getWasteCategory())) {
                        cell.setCellValue(plan.getWasteCategory());
                    }

                    // 固废类别列
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroup4Start && StringUtils.isNotEmpty(plan.getWasteType())) {
                        cell.setCellValue(plan.getWasteType());
                    }

                    // 固废代码列
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroup4Start && StringUtils.isNotEmpty(plan.getWasteCode())) {
                        cell.setCellValue(plan.getWasteCode());
                    }

                    // 以下列每行都显示
                    // 年份
                    CellUtils.setIntegerVal(row, cellIndex++, plan.getYear(), styleN0);
                    // 年度总量上限(t)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getAnnualLimit(), styleN3);
                    // 第一 季度上限(t)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getFirstLimit(), styleN3);
                    // 第二 季度上限(t)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getSecondLimit(), styleN3);
                    // 第三 季度上限(t)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getThirdLimit(), styleN3);
                    // 第四 季度上限(t)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getFourthLimit(), styleN3);
                    // 预警阈值(%)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getWarnVal(), styleN3);
                    // 告警阈值(%)
                    CellUtils.setDoubleVal(row, cellIndex++, plan.getAlarmVal(), styleN3);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, plan.getRemark(), style);
                    // ========== 处理第1层分组：企业（按 entCode）==========
                    group1Size++;
                    boolean isGroup1End = (i == list.size() - 1) ||
                            !curEntCode.equals(list.get(i + 1).getEntCode() == null ? "" : list.get(i + 1).getEntCode());
                    if (isGroup1End) {
                        if (group1Size > 1) {
                            // 合并企业名称列（列索引1）
                            mergeRegions.add(new CellRangeAddress(group1StartRow,
                                    group1StartRow + group1Size - 1, 1, 1));
                        }
                        // 重置组信息
                        group1StartRow = rowIndex;
                        group1Size = 0;
                    }
                    // ========== 处理第2层分组：排放口（按 outPutId）==========
                    group2Size++;
                    boolean isGroup2End = (i == list.size() - 1) || isGroup1End ||
                            !curOutPutId.equals(list.get(i + 1).getOutPutId() == null ? "" : list.get(i + 1).getOutPutId());
                    if (isGroup2End) {
                        if (group2Size > 1) {
                            // 合并序号列（列索引0）
                            mergeRegions.add(new CellRangeAddress(group2StartRow,
                                    group2StartRow + group2Size - 1, 0, 0));
                            // 合并归属排口code列（列索引2）
                            mergeRegions.add(new CellRangeAddress(group2StartRow,
                                    group2StartRow + group2Size - 1, 2, 2));
                            // 合并归属排口name列（列索引3）
                            mergeRegions.add(new CellRangeAddress(group2StartRow,
                                    group2StartRow + group2Size - 1, 3, 3));
                        }
                        // 重置组信息
                        group2StartRow = rowIndex;
                        group2Size = 0;
                        serialNo++; // 序号递增（按第2层分组：排放口）
                    }
                    // ========== 处理第3层分组：固废分类（按 wasteCategory）==========
                    group3Size++;
                    boolean isGroup3End = (i == list.size() - 1) || isGroup2End ||
                            !curWasteCategory.equals(list.get(i + 1).getWasteCategory() == null ? "" : list.get(i + 1).getWasteCategory());
                    if (isGroup3End) {
                        if (group3Size > 1) {
                            // 合并固废分类列（列索引4）
                            mergeRegions.add(new CellRangeAddress(group3StartRow,
                                    group3StartRow + group3Size - 1, 4, 4));
                        }
                        // 重置组信息
                        group3StartRow = rowIndex;
                        group3Size = 0;
                    }
                    // ========== 处理第4层分组：固废类别（按 wasteType）==========
                    group4Size++;
                    boolean isGroup4End = (i == list.size() - 1) || isGroup3End ||
                            !curWasteType.equals(list.get(i + 1).getWasteType() == null ? "" : list.get(i + 1).getWasteType());
                    if (isGroup4End) {
                        if (group4Size > 1) {
                            // 合并固废类别列（列索引5）
                            mergeRegions.add(new CellRangeAddress(group4StartRow,
                                    group4StartRow + group4Size - 1, 5, 5));
                            // 合并固废代码列（列索引6）
                            mergeRegions.add(new CellRangeAddress(group4StartRow,
                                    group4StartRow + group4Size - 1, 6, 6));
                        }
                        // 重置组信息
                        group4StartRow = rowIndex;
                        group4Size = 0;
                    }
                    // 更新前一行的值，用于下一轮循环判断
                    prevEntCode = curEntCode;
                    prevOutPutId = curOutPutId;
                    prevWasteCategory = curWasteCategory;
                    prevWasteType = curWasteType;
                }
                // 应用所有合并区域
                for (CellRangeAddress mergeRegion : mergeRegions) {
                    sheet.addMergedRegion(mergeRegion);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("固废总量控制列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<WasteTotalPlan> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 转换分类信息
        List<WasteDict> districts = wasteDictService.selectWasteDictList(null);
        Map<String, WasteDict> districtMap = new HashMap<>();
        districts.forEach( e -> districtMap.put(e.getId() + "", e));
        List<EntInfo> entList = redisCacheUtils.getAllEntList();
        Map<String, String> entMap = new HashMap<>();
        if (null != entList && !entList.isEmpty()) {
            entList.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getEntCode())) {
                    entMap.put(e.getEntCode(), e.getEntName());
                }
            });
        }
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        Map<String, OutPutInfo> outMap = new HashMap<>();
        if (null != outList && !outList.isEmpty()) {
            outList.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getOutPutId())) {
                    outMap.put(e.getOutPutId(), e);
                }
            });
        }
        list.forEach( e -> {
            e.setEntName(entMap.get(e.getEntCode()));
            OutPutInfo out = outMap.get(e.getOutPutId());
            if (null != out) {
                e.setOutPutCode(out.getOutPutCode());
                e.setOutPutName(out.getOutPutName());
            }
            if (StringUtils.isNotEmpty(e.getWasteDictId())) {
                String[] ids = e.getWasteDictId().split(",");
                WasteDict dict = districtMap.get(ids[0]);
                if (null != dict) {
                    e.setWasteCategory(dict.getName());
                    dict = districtMap.get(ids[ids.length - 1]);
                    if (null != dict) {
                        e.setWasteType(dict.getName());
                        e.setWasteCode(dict.getTag());
                    }
                }
            }
        });
    }

    private WasteLibReq initReq(WasteLibReq req) {
        if (null == req) {
            req = new WasteLibReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return null;
            }
        }
        return req;
    }

    @Override
    @Log(title = "固废总量控制计划", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteTotalPlan(WasteTotalPlan info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        int count = wasteTotalPlanMapper.insertWasteTotalPlan(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废总量控制计划", businessType = BusinessType.UPDATE)
    public AjaxResult updateWasteTotalPlan(WasteTotalPlan info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        int count = wasteTotalPlanMapper.updateWasteTotalPlan(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废总量控制计划", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteTotalPlan(WasteTotalPlan info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        // 判断是否被使用
        int count = wasteTotalPlanMapper.deleteWasteTotalPlan(info);
        return AjaxResult.success(count);
    }
}


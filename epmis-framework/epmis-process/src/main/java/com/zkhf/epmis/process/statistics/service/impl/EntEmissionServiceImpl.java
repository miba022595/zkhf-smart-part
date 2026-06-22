package com.zkhf.epmis.process.statistics.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.EntInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.domain.OutPutPollInfo;
import com.zkhf.epmis.process.base.domain.PollutantCode;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.ProcessTools;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.statistics.EntEmissionMapper;
import com.zkhf.epmis.process.statistics.domain.EmissionReq;
import com.zkhf.epmis.process.statistics.domain.EntEmission;
import com.zkhf.epmis.process.statistics.domain.OutEmission;
import com.zkhf.epmis.process.statistics.service.EntEmissionService;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业年排量信息记录Service业务层处理
 */
@Slf4j
@Service
public class EntEmissionServiceImpl implements EntEmissionService {

    private EntEmissionMapper entEmissionMapper;
    @Autowired
    public void setEntEmissionMapper(EntEmissionMapper entEmissionMapper) {
        this.entEmissionMapper = entEmissionMapper;
    }

    private ProcessTools processTools;
    @Autowired
    public void setProcessTools(ProcessTools processTools) {
        this.processTools = processTools;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectEntEmissionList(EmissionReq req) {
        if (null == req) {
            req = new EmissionReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页处理
        PageUtils.startPage();
        List<EntEmission> list = entEmissionMapper.selectEntEmissionList(req);
        // 填充数据
        fillData(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "企业年排量信息统计列表", businessType = BusinessType.EXPORT)
    public void exportEntEmission(EmissionReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EmissionReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业年排量数据列表.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntEmission> list = entEmissionMapper.selectEntEmissionList(req);
            if (null != list && !list.isEmpty()) {
                // 填充数据
                fillData(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle numStyle0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle numStyle4 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,4);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntEmission info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getEntName());
                    // 排放时间，年
                    cell = CellUtils.getCell(row, cellIndex++, numStyle0);
                    if (null != info.getEmissionYear()) {
                        cell.setCellValue(info.getEmissionYear());
                    }
                    // 污染因子编码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutantCode());
                    // 污染因子中文名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutantNameCn());
                    // 年排放量
                    cell = CellUtils.getCell(row, cellIndex++, numStyle4);
                    if (null != info.getEmissions()) {
                        cell.setCellValue(toDouble(info.getEmissions()));
                    }
                    // 年排放量限值
                    cell = CellUtils.getCell(row, cellIndex++, numStyle4);
                    if (null != info.getYLimit()) {
                        cell.setCellValue(toDouble(info.getYLimit()));
                    }
                    // 截至到当前月的放量限值
                    cell = CellUtils.getCell(row, cellIndex++, numStyle4);
                    if (null != info.getMLimit()) {
                        cell.setCellValue(toDouble(info.getMLimit()));
                    }
                    // 排放量单位
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getUnitPfCn());
                    // 报警类型
                    cell = CellUtils.getCell(row, cellIndex, style);
                    if (EntEmission.ALARM.equals(info.getAlarm())) {
                        cell.setCellValue("报警");
                    } else if (EntEmission.WARN.equals(info.getAlarm())) {
                        cell.setCellValue("预警");
                    } else {
                        cell.setCellValue("正常");
                    }
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业年排量数据列表.xlsx", StandardCharsets.UTF_8));
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

    private void fillData(List<EntEmission> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int montVal = now.getMonthValue();
        // 查询企业资质的排放量信息-年排放量限值
        Map<String, Map<String, BigDecimal>> entYLimitMap = processTools.getEntYLimitMap(year);
        // 排口的月排量限值统计，按企业统计
        Map<String, Map<String, BigDecimal>> entMLimitMap = new HashMap<>();
        outLimitDeal(montVal, entMLimitMap);
        // 获取污染物信息
        List<PollutantCode> codeList = redisCacheUtils.getAllPollDataList();
        Map<String, PollutantCode> codeMap = new HashMap<>();
        codeList.forEach( e -> codeMap.put(e.getPollutantCode(), e));
        // 获取企业信息
        List<EntInfo> entList = redisCacheUtils.getAllEntList();
        Map<String, EntInfo> entMap = new HashMap<>();
        entList.forEach( e -> entMap.put(e.getEntCode(), e));
        for (EntEmission info : list) {
            PollutantCode pollCode = codeMap.get(info.getPollutantCode());
            if (null != pollCode) {
                info.setPollutantNameCn(pollCode.getPollutantNameCn());
                info.setPollutantNameEn(pollCode.getPollutantNameEn());
                info.setUnitPfCn(pollCode.getUnitPfCn());
                info.setUnitPfEn(pollCode.getUnitPfEn());
            }
            EntInfo out = entMap.get(info.getEntCode());
            if (null == out) {
                continue;
            }
            info.setEntCode(out.getEntCode());
            info.setEntName(out.getEntName());
            if (null == info.getEmissions()) {
                continue;
            }
            info.setAlarm(EntEmission.NORMAL);
            // 企业的年排量限值
            Map<String, BigDecimal> yLimitMap = entYLimitMap.get(info.getEntCode());
            BigDecimal yLimit = null == yLimitMap ? null : yLimitMap.get(info.getPollutantCode());
            info.setYLimit(yLimit);
            if (null != yLimit && info.getEmissions().compareTo(yLimit) >= 0) {
                info.setAlarm(EntEmission.ALARM);
                continue;
            }
            // 企业的月排量限值
            Map<String, BigDecimal> mLimitMap = entMLimitMap.get(info.getEntCode());
            // 通过月份判断是否触发预警
            BigDecimal mLimit = null == mLimitMap ? null : mLimitMap.get(info.getPollutantCode());
            info.setMLimit(mLimit);
            if (null != mLimit && info.getEmissions().compareTo(mLimit) >= 0) {
                info.setAlarm(EntEmission.WARN);
            }
        }
    }

    private void outLimitDeal(Integer monthValue, Map<String, Map<String, BigDecimal>> entMLimitMap) {
        // 获取排口污染物信息-月排放量限值
        List<OutPutPollInfo> pollInfos = redisCacheUtils.getAllOutPutPollList();
        for (OutPutPollInfo e : pollInfos) {
            if (StringUtils.isEmpty(e.getOutPutId())) {
                continue;
            }
            // 获取截至到当月的排放量限值
            BigDecimal nowLimit = processTools.getToNowLimitValue(e, monthValue);
            if (null == nowLimit) {
                continue;
            }
            // 设置企业的(累加)
            Map<String, BigDecimal> entMonth = entMLimitMap.computeIfAbsent(e.getEntCode(), k -> new HashMap<>());
            entMonth.compute(e.getPollutantCode(), (k, old) -> null == old ? nowLimit : (nowLimit.add(old)));
        }
    }

    @Override
    public AjaxResult selectOutEmissionList(EmissionReq req) {
        if (null == req) {
            return AjaxResult.error("未知的查询参数");
        }
        if (StringUtils.isEmpty(req.getEntCode())) {
            return AjaxResult.success(new ArrayList<>());
        }
        List<OutEmission> list = entEmissionMapper.selectOutEmissionList(req);
        // 填充数据
        fillOutEmissionData(list, req);
        return AjaxResult.success(list);
    }

    private void fillOutEmissionData(List<OutEmission> list, EmissionReq req) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 获取排口信息
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        Map<String, OutPutInfo> outMap = new HashMap<>();
        outList.forEach( e -> outMap.put(e.getOutPutId(), e));
        // 获取污染物信息
        List<PollutantCode> codeList = redisCacheUtils.getAllPollDataList();
        Map<String, PollutantCode> codeMap = new HashMap<>();
        codeList.forEach( e -> codeMap.put(e.getPollutantCode(), e));
        for (OutEmission info : list) {
            OutPutInfo out = outMap.get(info.getOutPutId());
            if (null == out) {
                continue;
            }
            info.setOutPutCode(out.getOutPutCode());
            info.setOutPutName(out.getOutPutName());
            info.setOutPutType(out.getOutPutType());
            info.setOutPutTypeDesc(OutPutTypeEnum.getNameByCode(info.getOutPutType()));
            PollutantCode pollCode = codeMap.get(req.getPollutantCode());
            if (null != pollCode) {
                info.setUnitPfCn(pollCode.getUnitPfCn());
                info.setUnitPfEn(pollCode.getUnitPfEn());
            }
        }
    }

    private double toDouble(BigDecimal decimal) {
        if (null == decimal) {
            return 0;
        }
        return decimal.setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}


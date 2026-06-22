package com.zkhf.epmis.process.statistics.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.DataTypeEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.statistics.EffectiveTransMapper;
import com.zkhf.epmis.process.statistics.domain.EffectiveTransInfo;
import com.zkhf.epmis.process.statistics.domain.EffectiveTransReq;
import com.zkhf.epmis.process.statistics.service.EffectiveTransService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业传输有效率信息统计Service业务层处理
 */
@Slf4j
@Service
public class EffectiveTransServiceImpl implements EffectiveTransService {

    private EffectiveTransMapper effectiveTransMapper;
    @Autowired
    public void setEffectiveTransMapper(EffectiveTransMapper effectiveTransMapper) {
        this.effectiveTransMapper = effectiveTransMapper;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectEffectiveTransList(EffectiveTransReq req) {
        if (null == req) {
            req = new EffectiveTransReq();
        }
        // 请求参数处理
        Map<String, OutPutInfo> outPutMap = new HashMap<>();
        AjaxResult deal = process(req, outPutMap);
        if (null != deal) {
            return deal;
        }
        // 分页处理
        PageUtils.startPage();
        List<EffectiveTransInfo> list = effectiveTransMapper.selectEffectiveTransList(req);
        // 填充数据
        fillData(list, outPutMap);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "企业数据传输有效率信息统计列表", businessType = BusinessType.EXPORT)
    public void exportEffectiveTrans(EffectiveTransReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EffectiveTransReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业传输有效率模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 请求参数处理
            Map<String, OutPutInfo> outPutMap = new HashMap<>();
            AjaxResult deal = process(req, outPutMap);
            if (null != deal) {
                log.error("企业排口信息获取失败");
                return;
            }
            List<EffectiveTransInfo> list = effectiveTransMapper.selectEffectiveTransList(req);
            if (null != list && !list.isEmpty()) {
                // 填充数据
                fillData(list, outPutMap);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                CellStyle style2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 2);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EffectiveTransInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 所属企业
                    CellUtils.setStringVal(row, cellIndex++, info.getEntName(), style);
                    // 排口名称
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutName(), style);
                    // 排口类型
                    CellUtils.setStringVal(row, cellIndex++, OutPutTypeEnum.getNameByCode(info.getOutPutType()), style);
                    // 数据来源；1 小时传输、2 天传输
                    String dataTypeName = null;
                    if (DataTypeEnum.hour.code.equals(info.getDataType())) {
                        dataTypeName = "小时数据";
                    } else if (DataTypeEnum.day.code.equals(info.getDataType())) {
                        dataTypeName = "日数据";
                    }
                    CellUtils.setStringVal(row, cellIndex++, dataTypeName, style);
                    // 污染物监测时间，yyyy-MM-dd
                    CellUtils.setStringVal(row, cellIndex++, info.getMonitorDate(), style);
                    // 传输实收量
                    CellUtils.setIntegerVal(row, cellIndex++, info.getRealTrans(), style0);
                    // 传输应收量
                    CellUtils.setIntegerVal(row, cellIndex++, info.getMustTrans(), style0);
                    // 数据传输率
                    CellUtils.setFloatVal(row, cellIndex++, info.getTransRate(), style2);
                    // 有效实收量
                    CellUtils.setIntegerVal(row, cellIndex++, info.getRealValid(), style0);
                    // 有效应收量
                    CellUtils.setIntegerVal(row, cellIndex++, info.getMustValid(), style0);
                    // 数据有效率
                    CellUtils.setFloatVal(row, cellIndex++, info.getValidRate(), style2);
                    // 有效传输率
                    CellUtils.setFloatVal(row, cellIndex, info.getEffTranRate(), style2);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业传输有效率.xlsx", StandardCharsets.UTF_8));
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

    private void fillData(List<EffectiveTransInfo> list, Map<String, OutPutInfo> outPutMap) {
        if (null == list || list.isEmpty()) {
            return;
        }
        list.forEach(e -> {
            // 计算传输率和有效率
            e.setTransRate(computeData(e.getRealTrans(), e.getMustTrans()));
            e.setValidRate(computeData(e.getRealValid(), e.getMustValid()));
            e.setEffTranRate(effectiveTransRate(e.getTransRate(), e.getValidRate()));
            if (null != e.getMonitorTime()) {
                if (DataTypeEnum.hour.code.equals(e.getDataType())) {
                    e.setMonitorDate(e.getMonitorTime().format(DateUtils.yy_m_d_h));
                } else if (DataTypeEnum.day.code.equals(e.getDataType())) {
                    e.setMonitorDate(e.getMonitorTime().format(DateUtils.yy_m_d));
                }
            }
            OutPutInfo info = outPutMap.get(e.getOutPutId());
            if (null != info) {
                e.setEntCode(info.getEntCode());
                e.setEntName(info.getEntName());
                e.setOutPutCode(info.getOutPutCode());
                e.setOutPutName(info.getOutPutName());
                e.setOutPutType(info.getOutPutType());
                e.setOutPutTypeDesc(OutPutTypeEnum.getNameByCode(e.getOutPutType()));
                if (null != e.getTransRate() && null != info.getTransRate()) {
                    if (Float.compare(info.getTransRate(), e.getTransRate()) > 0) { // 小于指定传输率时报警
                        e.setTransAlarm(EffectiveTransInfo.ALARM);
                    } else {
                        e.setTransAlarm(EffectiveTransInfo.NORMAL);
                    }
                }
                if (null != e.getValidRate() && null != info.getValidRate()) {
                    if (Float.compare(info.getValidRate(), e.getValidRate()) > 0) { // 小于指定有效率时报警
                        e.setValidAlarm(EffectiveTransInfo.ALARM);
                    } else {
                        e.setTransAlarm(EffectiveTransInfo.NORMAL);
                    }
                }
                // 规定的有效传输率
                Float infoEffTranRate = effectiveTransRate(info.getTransRate(), info.getValidRate());
                if (null != e.getEffTranRate() && null != infoEffTranRate) {
                    if (Float.compare(infoEffTranRate, e.getEffTranRate()) > 0) {
                        e.setEffTranAlarm(EffectiveTransInfo.ALARM);
                    } else {
                        e.setEffTranAlarm(EffectiveTransInfo.NORMAL);
                    }
                }
            }
        });
    }

    private AjaxResult process(EffectiveTransReq req, Map<String, OutPutInfo> outPutMap) {
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 时间转换，起止实际同时存在
        if (StringUtils.isNotEmpty(req.getBeginTime()) && StringUtils.isNotEmpty(req.getEndTime())) {
            req.setBeginTime(req.getBeginTime() + " 00:00:00");
            req.setEndTime(req.getEndTime() + " 23:59:59");
        } else {
            req.setBeginTime(null);
            req.setEndTime(null);
        }
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        if (null == outPutList || outPutList.isEmpty()) {
            return AjaxResult.error("未获取到权限列表");
        }
        List<String> outPutIdList = new ArrayList<>();
        for (OutPutInfo info : outPutList) {
            if (null == info.getEntCode() || null == info.getOutPutId()) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getEntCodes()) && !req.getEntCodes().contains(info.getEntCode())) {
                continue;
            }
            if (null != req.getOutPutType() && !req.getOutPutType().equals(info.getOutPutType())) {
                continue;
            }
            if (null != req.getOutPutIdList() && !req.getOutPutIdList().isEmpty() && !req.getOutPutIdList().contains(info.getOutPutId())) {
                continue;
            }
            outPutMap.put(info.getOutPutId(), info);
            outPutIdList.add(info.getOutPutId());
        }
        // 筛选为空后返回空列表
        if (outPutIdList.isEmpty()) {
            return AjaxResult.success(new ArrayList<>());
        }
        req.setOutPutIdList(outPutIdList);
        return null;
    }

    private Float computeData(Integer real, Integer must) {
        if (null == real || null == must) {
            return null;
        }
        if (must < 1) {
            return 0.0f;
        }
        return Math.round(real * 10000.0 / must) / 100.0f;
    }

    private Float effectiveTransRate(Float validRate, Float transRate) {
        if (null == validRate || null == transRate) {
            return null;
        }
        return Math.round(validRate * transRate) / 100.0f;
    }
}


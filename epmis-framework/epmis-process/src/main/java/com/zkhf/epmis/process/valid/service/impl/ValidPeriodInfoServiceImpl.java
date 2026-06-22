package com.zkhf.epmis.process.valid.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AlarmTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.ValidPeriodTypeEnum;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.process.base.domain.EntInfo;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.base.CursorMapper;
import com.zkhf.epmis.process.mapper.valid.ValidPeriodInfoMapper;
import com.zkhf.epmis.process.valid.domain.ValidPeriodInfo;
import com.zkhf.epmis.process.valid.domain.ValidPeriodReq;
import com.zkhf.epmis.process.valid.service.ValidPeriodInfoService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业资质有效期预警数据Service业务层处理
 */
@Slf4j
@Service
public class ValidPeriodInfoServiceImpl implements ValidPeriodInfoService {

    private ValidPeriodInfoMapper validPeriodInfoMapper;
    @Autowired
    public void setValidPeriodInfoMapper(ValidPeriodInfoMapper validPeriodInfoMapper) {
        this.validPeriodInfoMapper = validPeriodInfoMapper;
    }

    private CursorMapper cursorMapper;
    @Autowired
    public void setCursorMapper(CursorMapper cursorMapper) {
        this.cursorMapper = cursorMapper;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectConfType() {
        return AjaxResult.success(ValidPeriodTypeEnum.getAll());
    }

    @Override
    public AjaxResult selectValidPeriodInfoList(ValidPeriodReq req) {
        if (null == req) {
            req = new ValidPeriodReq();
        }
        try {
            // 企业信息临时表处理
            Map<String, EntInfo> entInfoMap = new HashMap<>();
            AjaxResult deal = temporaryDeal(req, entInfoMap);
            if (null != deal) {
                return deal;
            }
            PageUtils.startPage();
            List<ValidPeriodInfo> list = validPeriodInfoMapper.selectValidPeriodInfoList(req);
            fillData(list, entInfoMap);
            return PageUtils.getAjaxResult(list, true);
        } finally {
            if (req.isTemporary()) { // 移除临时表
                cursorMapper.dropTempTable(req.getTableName());
            }
        }
    }

    @Override
    @Log(title = "企业资质有效期预警数据", businessType = BusinessType.EXPORT)
    public void exportValidPeriodInfo(ValidPeriodReq req, HttpServletResponse response) {
        if (null == req) {
            req = new ValidPeriodReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业资质有效期预警模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 企业信息临时表处理
            Map<String, EntInfo> entInfoMap = new HashMap<>();
            AjaxResult deal = temporaryDeal(req, entInfoMap);
            if (null != deal) {
                log.error("企业信息获取失败");
                return;
            }
            List<ValidPeriodInfo> list = validPeriodInfoMapper.selectValidPeriodInfoList(req);
            if (null != list && !list.isEmpty()) {
                fillData(list, entInfoMap);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行
                // 获取数字单元格格式
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (ValidPeriodInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, info.getEntName(), style);
                    // 资质证件
                    CellUtils.setStringVal(row, cellIndex++,
                            StringUtils.isEmpty(info.getItemName()) ? info.getConfDesc() : info.getConfDesc() + "-" + info.getItemName(),
                            style);
                    // 剩余有效天数
                    CellUtils.setLongVal(row, cellIndex++, info.getLeftDays(), style0);
                    // 资质有效期-开始时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getBeginDate(), DateUtils.yy_m_d, style);
                    // 资质有效期-结束时间
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getEndDate(), DateUtils.yy_m_d, style);
                    // 上次发送报警的时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex, info.getSendTime(), DateUtils.yy_m_d_h_m_s, style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业资质有效期预警.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (req.isTemporary()) { // 移除临时表
                cursorMapper.dropTempTable(req.getTableName());
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("outputStream close", e);
                }
            }
        }
    }

    private AjaxResult temporaryDeal(ValidPeriodReq req, Map<String, EntInfo> entInfoMap) {
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 获取权限下的企业和排口信息
        List<EntInfo> entList = redisCacheUtils.getAllEntList(null);
        if (null == entList) {
            return AjaxResult.error("未获取到权限列表");
        }
        if (entList.isEmpty()) {
            return AjaxResult.success();
        }
        req.setEntList(entList);
        entList.forEach(e -> entInfoMap.put(e.getEntCode(), e));
        // 参数大小控制，超过100条时采用临时表方式
        req.setTemporary(entList.size() >= 100);
        if (req.isTemporary()) { // 构建临时表
            // 生成表名
            String tableName = UlidCreator.getMonotonicUlid().toString();
            req.setTableName(tableName);
            // 1. 创建临时表
            cursorMapper.createEntDynamicTable(tableName);
            // 2. 批量插入数据（分批处理避免大事务）
            int batchSize = 1000;
            for (int i = 0; i < entList.size(); i += batchSize) {
                List<EntInfo> batch = entList.subList(i, Math.min(i + batchSize, entList.size()));
                cursorMapper.batchInsertEntDynamicData(batch, tableName);
            }
        }
        return null;
    }

    private void fillData(List<ValidPeriodInfo> list, Map<String, EntInfo> entInfoMap) {
        if (null == list || list.isEmpty()) {
            return;
        }
        list.forEach(e -> {
            if (null != entInfoMap && entInfoMap.containsKey(e.getEntCode())) { // 设置企业名称
                e.setEntName(entInfoMap.get(e.getEntCode()).getEntName());
            }
            e.setConfDesc(ValidPeriodTypeEnum.getDescByCode(e.getConfType()));
            e.setAlarmTypeDesc(AlarmTypeEnum.getNameByCode(e.getAlarmType()));
        });
    }
}


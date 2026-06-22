package com.zkhf.epmis.platform.ops.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ops.OpsAttRecordMapper;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecord;
import com.zkhf.epmis.platform.ops.domain.OpsAttRecordReq;
import com.zkhf.epmis.platform.ops.domain.OpsClock;
import com.zkhf.epmis.platform.ops.service.OpsAttRecordService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤打卡记录Service业务层处理
 */
@Slf4j
@Service
public class OpsAttRecordServiceImpl implements OpsAttRecordService {

    private OpsAttRecordMapper opsAttRecordMapper;
    @Autowired
    public void setOpsAttRecordMapper(OpsAttRecordMapper opsAttRecordMapper) {
        this.opsAttRecordMapper = opsAttRecordMapper;
    }

    @Override
    public AjaxResult selectOpsAttRecordList(OpsAttRecordReq req) {
        // 请求参数转换
        req = initOpsAttRecordReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 运维配置列表查询
        PageUtils.startPage();
        List<OpsAttRecord> list = opsAttRecordMapper.selectOpsAttRecordList(req);
        // 填充信息
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "考勤打卡记录", businessType = BusinessType.EXPORT)
    public void exportOpsAttRecord(OpsAttRecordReq req, HttpServletResponse response) {
        if (null == req) {
            req = new OpsAttRecordReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("考勤打卡记录模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 请求参数转换
            req = initOpsAttRecordReq(req);
            List<OpsAttRecord> list;
            if (null == req) {
                list = new ArrayList<>();
            } else {
                list = opsAttRecordMapper.selectOpsAttRecordList(req);
            }
            // 填充信息
            fill(list);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowNm = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowNm);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowNm, list.size());
                for (OpsAttRecord record : list) {
                    row = sheet.createRow(rowNm++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style);
                    // 姓名
                    if (StringUtils.isEmpty(record.getNickName())) {
                        CellUtils.setStringVal(row, cellIndex++, record.getUserName(), style);
                    } else {
                        CellUtils.setStringVal(row, cellIndex++, record.getNickName(), style);
                    }
                    // 运维企业
                    CellUtils.setStringVal(row, cellIndex++, record.getOpsUnitName(), style);
                    // 打卡企业
                    CellUtils.setStringVal(row, cellIndex++, record.getEntName(), style);
                    // 打卡排口编码
                    CellUtils.setStringVal(row, cellIndex++, record.getOutPutCode(), style);
                    // 打卡排口
                    CellUtils.setStringVal(row, cellIndex++, record.getOutPutName(), style);
                    // 签到时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex++, record.getPunchTimeIn(), DateUtils.yy_m_d_h_m_s, style);
                    // 签到定位信息
                    CellUtils.setStringVal(row, cellIndex++, record.getPunchLocationIn(), style);
                    // 签退时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex++, record.getPunchTimeOut(), DateUtils.yy_m_d_h_m_s, style);
                    // 签退定位信息
                    CellUtils.setStringVal(row, cellIndex++, record.getPunchLocationOut(), style);
                    // 打卡时长
                    CellUtils.setStringVal(row, cellIndex++, record.getPunchDurationDesc(), style);
                    // 协助人
                    if (StringUtils.isEmpty(record.getAssistantNick())) {
                        CellUtils.setStringVal(row, cellIndex++, record.getAssistantName(), style);
                    } else {
                        CellUtils.setStringVal(row, cellIndex++, record.getAssistantNick(), style);
                    }
                    // 协助说明
                    CellUtils.setStringVal(row, cellIndex, record.getAssistantRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("考勤打卡记录列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<OpsAttRecord> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 打卡时间转成字符串格式，按天、时、分最大单位拆分
        list.forEach(e -> e.setPunchDurationDesc(DateUtils.convertMinutes(e.getPunchDuration())));
    }

    private OpsAttRecordReq initOpsAttRecordReq(OpsAttRecordReq req) {
        if (null == req) {
            req = new OpsAttRecordReq();
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
    public AjaxResult selectNewestOpsAttRecord(OpsAttRecordReq req) {
        req.setUserId(GVarContainer.getUserId());
        OpsAttRecord record = opsAttRecordMapper.selectNewestOpsAttRecord(req);
        // 上次未签退时，返回记录id，表示需要签退, 返回空，表示开启新的签到
        Object val;
        if (null != record && null == record.getPunchTimeOut()) {
            val = record.getRecordId();
        } else {
            val = "";
        }
        return AjaxResult.success(val);
    }


    @Override
    @Log(title = "考勤打卡", businessType = BusinessType.UPDATE)
    public AjaxResult opsAttClock(OpsClock info) {
        info.setUserId(GVarContainer.getUserId());
        info.setPunchTime(LocalDateTime.now());
        int count;
        if (StringUtils.isEmpty(info.getRecordId())) { // 记录ID为空，表示签到
            info.setRecordId(UlidCreator.getMonotonicUlid().toString());
            count = opsAttRecordMapper.opsAttPunchIn(info);
        } else { // 记录ID不为空，表示签退
            count = opsAttRecordMapper.opsAttPunchOut(info);
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "考勤打卡校准", businessType = BusinessType.UPDATE)
    public AjaxResult opsAttAssistant(OpsAttRecord info) {
        info.setAssistant(GVarContainer.getUserId());
        info.setPunchTimeOut(LocalDateTime.now());
        int count = opsAttRecordMapper.calibrateOpsAttRecord(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "考勤打卡记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteOpsAttRecordById(String recordId) {
        OpsAttRecord record = opsAttRecordMapper.selectOpsAttRecordById(recordId);
        if (null == record) {
            return AjaxResult.error("未知的打卡记录");
        }
        int count = opsAttRecordMapper.deleteOpsAttRecordById(recordId);
        return AjaxResult.success(count);
    }
}


package com.zkhf.epmis.platform.ent.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.platform.ent.domain.DeviceInfo;
import com.zkhf.epmis.platform.ent.service.DeviceInfoService;
import com.zkhf.epmis.platform.enums.DeviceLifeUnitEnum;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.DeviceInfoMapper;
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
import java.util.List;

/**
 * 设备信息Service业务层处理
 */
@Slf4j
@Service
public class DeviceInfoServiceImpl implements DeviceInfoService {

    private DeviceInfoMapper deviceInfoMapper;
    @Autowired
    public void setDeviceInfoMapper(DeviceInfoMapper deviceInfoMapper) {
        this.deviceInfoMapper = deviceInfoMapper;
    }

    @Override
    public AjaxResult selectDeviceInfoByMnNum(String mnNum) {
        return AjaxResult.success(deviceInfoMapper.selectDeviceInfoByMnNum(mnNum));
    }

    @Override
    public AjaxResult selectDeviceInfoList() {
        return AjaxResult.success(getDeviceList());
    }

    @Override
    public void exportDeviceInfo(HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("监测设备信息模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<DeviceInfo> list = getDeviceList();
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 1;// 从第2行开始插入

                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle numStyle = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (DeviceInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 设备mn编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMnNum());
                    // 设备mn名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMnName());
                    // 品牌
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getDeviceBrand());
                    // 型号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getDeviceModel());
                    // 设备数量
                    cell = CellUtils.getCell(row, cellIndex++, numStyle);
                    cell.setCellValue(info.getDeviceQuantity());
                    // 安装时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(null == info.getSetupTime() ? "" : info.getSetupTime().format(DateUtils.yy_m_d_h_m_s));
                    // 寿命
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getLifespan() + DeviceLifeUnitEnum.getNameByCode(info.getLifeUnit()));
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("监测设备信息.xlsx", StandardCharsets.UTF_8));
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

    private List<DeviceInfo> getDeviceList() {
        List<DeviceInfo> infoList;
        if (GVarContainer.isAdmin()) {
            infoList = deviceInfoMapper.selectDeviceInfoList(null);
        } else {
            infoList = deviceInfoMapper.selectDeviceInfoList(GVarContainer.getEntCodes());
        }
        return infoList;
    }

    @Override
    public AjaxResult insertOrUpdateDeviceInfo(DeviceInfo info) {
        return AjaxResult.success(deviceInfoMapper.insertOrUpdateDeviceInfo(info));
    }
}

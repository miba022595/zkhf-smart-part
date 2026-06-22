package com.zkhf.epmis.platform.ledger.service.impl;

import com.zkhf.epmis.core.config.EPMISConfig;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.RelateEnum;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.enums.DeviceLifeUnitEnum;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.ledger.domain.GovernFacility;
import com.zkhf.epmis.platform.ledger.domain.MonitorFacility;
import com.zkhf.epmis.platform.ledger.service.EntLedgerService;
import com.zkhf.epmis.platform.mapper.ledger.EntLedgerMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业台账 Service业务层处理
 */
@Slf4j
@Service
public class EntLedgerServiceImpl implements EntLedgerService {

    private EntLedgerMapper entLedgerMapper;

    @Autowired
    public void setDeviceInfoMapper(EntLedgerMapper entLedgerMapper) {
        this.entLedgerMapper = entLedgerMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult monitorFacilityList() {
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
        }
        PageUtils.startPage();
        List<MonitorFacility> list = entLedgerMapper.monitorFacilityList(entCodes, RelateEnum.outPut.name());
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void monitorFacilityExport(HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业监测设施台账模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<String> entCodes = null;
            if (GVarContainer.isNotAdmin()) {
                entCodes = GVarContainer.getEntCodes();
            }
            List<MonitorFacility> list = entLedgerMapper.monitorFacilityList(entCodes, RelateEnum.outPut.name());
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 从第（rowIndex + 1）行开始插入

                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle numStyle = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                Cell cell;
                List<CellRangeAddress> mergeRegions = new ArrayList<>();

                int groupStartRow = rowIndex;
                int groupSize = 0;
                String prevMnNum = null;

                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (int i = 0; i < list.size(); i++) {
                    MonitorFacility info = list.get(i);

                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;

                    // 判断是否是组的开始(标记只在组的起始行显示)
                    boolean isGroupStart = !info.getMnNum().equals(prevMnNum);
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(index);
                    }
                    // 设备mn编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getMnNum());
                    }
                    // 设备mn名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getMnName());
                    }
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getEntName());
                    }
                    // 品牌
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getDeviceBrand());
                    }
                    // 型号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getDeviceModel());
                    }
                    // 设备数量
                    cell = CellUtils.getCell(row, cellIndex++, numStyle);
                    if (isGroupStart && null != info.getDeviceQuantity()) {
                        cell.setCellValue(info.getDeviceQuantity());
                    }
                    // 安装时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart && null != info.getSetupTime()) {
                        cell.setCellValue(info.getSetupTime().format(DateUtils.yy_m_d_h_m_s));
                    }
                    // 寿命
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart && null != info.getLifespan()) {
                        cell.setCellValue(info.getLifespan() + DeviceLifeUnitEnum.getNameByCode(info.getLifeUnit()));
                    }

                    // 其他列每行都显示
                    // 治理设施编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getFacilityCode());
                    // 治理设施名称
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getFacilityName());

                    groupSize++;

                    // 判断是否是组的结束
                    boolean isGroupEnd = (i == list.size() - 1) ||
                            !info.getMnNum().equals(list.get(i + 1).getMnNum());

                    if (isGroupEnd) {
                        // 添加合并区域（如果组有多行）
                        if (groupSize > 1) {
                            for (int col = 0; col <= 8; col++) {
                                mergeRegions.add(new CellRangeAddress(groupStartRow, groupStartRow + groupSize - 1, col, col));
                            }
                        }
                        // 重置组信息
                        groupStartRow = rowIndex;
                        groupSize = 0;
                        index++; // 序号递增
                    }
                    prevMnNum = info.getMnNum();
                }
                // 应用合并区域
                for (CellRangeAddress mergeRegion : mergeRegions) {
                    sheet.addMergedRegion(mergeRegion);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业监测设施台账.xlsx", StandardCharsets.UTF_8));
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

    @Override
    public AjaxResult governFacilityList() {
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
        }
        PageUtils.startPage();
        List<GovernFacility> list = entLedgerMapper.governFacilityList(entCodes, RelateEnum.outPut.name());
        // 填充工艺图
        if (null!= list && !list.isEmpty()) {
            // 获取所有的附件列表
            Map<String, List<AnnexInfo>> allImages = new HashMap<>();
            getAllImages(null, allImages);
            list.forEach( e -> e.setAnnexInfoList(allImages.remove(e.getFacilityId())));
        }
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public void governFacilityExport(HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业治理设施台账模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<String> entCodes = null;
            if (GVarContainer.isNotAdmin()) {
                entCodes = GVarContainer.getEntCodes();
            }
            List<GovernFacility> list = entLedgerMapper.governFacilityList(entCodes, RelateEnum.outPut.name());
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 从第（rowIndex + 1）行开始插入
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle numStyle = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                Cell cell;
                List<CellRangeAddress> mergeRegions = new ArrayList<>();

                int groupStartRow = rowIndex;
                int groupSize = 0;
                String prevFacilityCode = null;
                // 存储图片插入信息：行号 -> 图片路径列表
                Map<Integer, List<String>> imageInsertMap = new HashMap<>();
                // 获取所有的附件列表
                Map<String, List<String>> allImages = new HashMap<>();
                getAllImages(allImages, null);
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (int i = 0; i < list.size(); i++) {
                    GovernFacility info = list.get(i);

                    row = sheet.createRow(rowIndex);

                    int cellIndex = 0;

                    // 判断是否是组的开始
                    boolean isGroupStart = !info.getFacilityCode().equals(prevFacilityCode);

                    // 序号 - 只在组的起始行显示
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(index);
                    }

                    // 治理设施编号 - 只在组的起始行显示
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getFacilityCode());
                    }

                    // 治理设施名称 - 只在组的起始行显示
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getFacilityName());
                    }

                    // 企业名称 - 只在组的起始行显示
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (isGroupStart) {
                        cell.setCellValue(info.getEntName());
                    }
                    // 其他列每行都显示
                    // 设备mn名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMnName());
                    // 设备数量
                    cell = CellUtils.getCell(row, cellIndex++, numStyle);
                    if (null != info.getDeviceQuantity()) {
                        cell.setCellValue(info.getDeviceQuantity());
                    }
                    // 安装时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getSetupTime()) {
                        cell.setCellValue(info.getSetupTime().format(DateUtils.yy_m_d_h_m_s));
                    }
                    // 寿命
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != info.getLifespan()) {
                        cell.setCellValue(info.getLifespan() + DeviceLifeUnitEnum.getNameByCode(info.getLifeUnit()));
                    }
                    // 治理工艺图
                    cell = CellUtils.getCell(row, cellIndex, style);
                    if (isGroupStart) {
                        // 获取多个图片路径
                        List<String> imagePaths = allImages.get(info.getFacilityId());
                        if (null != imagePaths && !imagePaths.isEmpty()) {
                            // 记录需要插入图片的行和路径列表
                            imageInsertMap.put(rowIndex, imagePaths);
                            cell.setCellValue("共" + imagePaths.size() + "张图片");
                        } else {
                            cell.setCellValue("无图片");
                        }
                    }

                    rowIndex++;
                    groupSize++;

                    // 判断是否是组的结束
                    boolean isGroupEnd = (i == list.size() - 1) ||
                            !info.getFacilityCode().equals(list.get(i + 1).getFacilityCode());

                    if (isGroupEnd) {
                        // 添加合并区域（如果组有多行）
                        if (groupSize > 1) {
                            for (int col = 0; col <= 4; col++) {
                                mergeRegions.add(new CellRangeAddress(groupStartRow, groupStartRow + groupSize - 1, col, col));
                            }
                        }
                        // 重置组信息
                        groupStartRow = rowIndex;
                        groupSize = 0;
                        index++; // 序号递增
                    }
                    prevFacilityCode = info.getFacilityCode();
                }

                // 应用合并区域
                for (CellRangeAddress mergeRegion : mergeRegions) {
                    sheet.addMergedRegion(mergeRegion);
                }
                // 在所有数据写入后插入图片
                insertMultipleProcessImages(workbook, sheet, imageInsertMap, 8); // 4是治理工艺图列的索引
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业治理设施台账.xlsx", StandardCharsets.UTF_8));
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

    private void getAllImages(Map<String, List<String>> strImages, Map<String, List<AnnexInfo>> infoImages) {
        AnnexReq req = new AnnexReq();
        req.setSourceType(AnnexTypeEnum.entPollControlFacility.name());

        List<AnnexInfo> annexList = annexService.selectAnnexList(req);
        for (AnnexInfo info : annexList) {
            if (StringUtils.isEmpty(info.getFilePath())) {
                continue;
            }
            if ("png".equals(info.getFileType()) || "jpg".equals(info.getFileType())
                    || "jpeg".equals(info.getFileType()) || "gif".equals(info.getFileType())) {
                if (null != strImages) {
                    if (!strImages.containsKey(info.getSourceId())) {
                        strImages.put(info.getSourceId(), new ArrayList<>());
                    }
                    strImages.get(info.getSourceId()).add(info.getFilePath().substring(Constants.RESOURCE_PREFIX.length()));
                }
                if (null != infoImages) {
                    if (!infoImages.containsKey(info.getSourceId())) {
                        infoImages.put(info.getSourceId(), new ArrayList<>());
                    }
                    infoImages.get(info.getSourceId()).add(info);
                }
            }
        }
    }

    /**
     * 插入多个治理工艺图片到Excel（横向排列）
     */
    private void insertMultipleProcessImages(XSSFWorkbook workbook, Sheet sheet,
                                             Map<Integer, List<String>> imageInsertMap, int startColumnIndex) {
        Drawing<?> drawing = sheet.createDrawingPatriarch();

        for (Map.Entry<Integer, List<String>> entry : imageInsertMap.entrySet()) {
            int rowIndex = entry.getKey();
            List<String> imagePaths = entry.getValue();

            if (imagePaths == null || imagePaths.isEmpty()) {
                continue;
            }
            Row row = sheet.getRow(rowIndex);

            // 设置固定行高（适用于横向排列）
            row.setHeightInPoints(120); // 固定高度，适合大多数图片

            for (int i = 0; i < imagePaths.size(); i++) {
                String imagePath = EPMISConfig.getProfile() + imagePaths.get(i);
                try {
                    if (new File(imagePath).exists()) {
                        byte[] imageData = Files.readAllBytes(Paths.get(imagePath));
                        int pictureType = getPictureType(imageData);
                        int pictureIdx = workbook.addPicture(imageData, pictureType);

                        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                        anchor.setCol1(startColumnIndex + i);
                        anchor.setRow1(rowIndex);
                        anchor.setCol2(startColumnIndex + i + 1);
                        anchor.setRow2(rowIndex + 1);
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

                        Picture picture = drawing.createPicture(anchor, pictureIdx);
                        picture.resize(0.85); // 统一缩放

                        sheet.setColumnWidth(startColumnIndex + i, 18 * 256); // 固定列宽
                    }
                } catch (Exception e) {
                    log.warn("插入图片失败: {}", imagePath, e);
                }
            }
        }
    }

    /**
     * 根据图片数据判断图片类型
     */
    private int getPictureType(byte[] imageData) {
        if (imageData.length > 8) {
            // JPEG
            if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                return Workbook.PICTURE_TYPE_JPEG;
            }
            // PNG
            if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50 &&
                    imageData[2] == (byte) 0x4E && imageData[3] == (byte) 0x47) {
                return Workbook.PICTURE_TYPE_PNG;
            }
        }
        return Workbook.PICTURE_TYPE_JPEG;
    }

}

package com.zkhf.epmis.platform.envFee.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envFee.dict.FeeStatusDict;
import com.zkhf.epmis.platform.envFee.dict.FeeTypeDict;
import com.zkhf.epmis.platform.envFee.domain.EnvFee;
import com.zkhf.epmis.platform.envFee.domain.EnvFeeReq;
import com.zkhf.epmis.platform.envFee.service.EnvFeeDictService;
import com.zkhf.epmis.platform.envFee.service.EnvFeeService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envFee.EnvFeeMapper;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环保费用登记Service业务层处理
 */
@Slf4j
@Service
public class EnvFeeServiceImpl implements EnvFeeService {

    private EnvFeeMapper envFeeMapper;
    @Autowired
    public void setEnvFeeMapper(EnvFeeMapper envFeeMapper) {
        this.envFeeMapper = envFeeMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private EnvFeeDictService envFeeDictService;
    @Autowired
    public void setEnvFeeDictService(EnvFeeDictService envFeeDictService) {
        this.envFeeDictService = envFeeDictService;
    }

    @Override
    public AjaxResult selectEnvFeeList(EnvFeeReq req) {
        if (null == req) {
            req = new EnvFeeReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EnvFee> list = envFeeMapper.selectEnvFeeList(req);
        // 数据填充
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "环保费用登记", businessType = BusinessType.EXPORT)
    public void exportEnvFee(EnvFeeReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvFeeReq();
        }
        OutputStream outputStream = null;
        try {
            String templatePath = "template/环保费用列表模板.xlsx";
            InputStream fis = getClass().getClassLoader().getResourceAsStream(templatePath);
            if (fis == null) {
                log.error("无法从路径加载资源 {}", templatePath);
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvFee> list = envFeeMapper.selectEnvFeeList(req);
            // 数据填充
            fill(list);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 2;// 首行

            CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
            CellStyle numStyle2 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 2);
            if (null != list && !list.isEmpty()) {
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvFee fee : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 归属企业
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(fee.getEntName());
                    // 关联项目编码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(fee.getProjectCode());
                    // 关联项目名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(fee.getProjectName());
                    // 费用编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(fee.getFeeCode());
                    // 费用类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(fee.getFeeTypeName());
                    // 费用金额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != fee.getFeeAmount()) {
                        cell.setCellValue(fee.getFeeAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 开票金额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != fee.getInvoiceAmount()) {
                        cell.setCellValue(fee.getInvoiceAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 付款金额
                    cell = CellUtils.getCell(row, cellIndex++, numStyle2);
                    if (null != fee.getPaymentAmount()) {
                        cell.setCellValue(fee.getPaymentAmount().setScale(2, RoundingMode.HALF_UP).doubleValue());
                    }
                    // 缴费截至日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null != fee.getPaymentDate()) {
                        cell.setCellValue(fee.getPaymentDate().format(DateUtils.yy_m_d));
                    }
                    // 费用状态
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(fee.getStatusName());
                    // 费用描述
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(fee.getFeeDesc());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保费用列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<EnvFee> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 费用类型
        List<FeeTypeDict> feeTypeDictList = envFeeDictService.selectAllFeeType();
        Map<String, FeeTypeDict> feeTypeDictMap = new HashMap<>();
        feeTypeDictList.forEach( e -> feeTypeDictMap.put(e.getTypeCode(), e));
        // 费用状态
        List<FeeStatusDict> feeStatusDictList = envFeeDictService.selectAllFeeStatus();
        Map<String, FeeStatusDict> feeStatusDictMap = new HashMap<>();
        feeStatusDictList.forEach( e -> feeStatusDictMap.put(e.getStatusCode(), e));
        list.forEach( e -> {
            FeeTypeDict feeTypeDict = feeTypeDictMap.get(e.getFeeType());
            if (null != feeTypeDict) {
                e.setFeeTypeName(feeTypeDict.getTypeName());
                e.setFeeTypeDesc(feeTypeDict.getDescription());
            }
            FeeStatusDict feeStatusDict = feeStatusDictMap.get(e.getStatus());
            if (null != feeStatusDict) {
                e.setStatusName(feeStatusDict.getStatusName());
                e.setStatusDesc(feeStatusDict.getDescription());
            }
        });
    }

    @Override
    @Log(title = "环保费用登记", businessType = BusinessType.INSERT)
    public AjaxResult insertEnvFee(EnvFee envFee) {
        if (StringUtils.isEmpty(envFee.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        envFee.setFeeId(UlidCreator.getMonotonicUlid().toString());
        int count = envFeeMapper.insertEnvFee(envFee);
        if (count > 0 && null != envFee.getAnnexIds() && !envFee.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(envFee.getFeeId(), AnnexTypeEnum.envFees.name(), envFee.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环保费用登记", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvFee(EnvFee envFee) {
        int count = envFeeMapper.updateEnvFee(envFee);
        if (count > 0) {
            annexService.updateAnnex(envFee.getFeeId(), AnnexTypeEnum.envFees.name(), envFee.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环保费用登记", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnvFeeById(String feeId) {
        if (StringUtils.isEmpty(feeId)) {
            return AjaxResult.error("请求参数为空！");
        }
        int count = envFeeMapper.selectFeePaymentCountByFeeId(feeId);
        if (count > 0) {
            return AjaxResult.error("已存在付款信息，不允许删除！");
        }
        count = envFeeMapper.selectFeeInvoiceCountByFeeId(feeId);
        if (count > 0) {
            return AjaxResult.error("已存在发票信息，不允许删除！");
        }
        count = envFeeMapper.deleteEnvFeeById(feeId);
        if (count > 0) {
            // 删除附件
            annexService.updateAnnex(feeId, AnnexTypeEnum.envFees.name(), null);
        }
        return AjaxResult.success(count);
    }

    @Override
    public void updateEnvFeeInvoiceAmount(String feeId) {
        envFeeMapper.updateEnvFeeInvoiceAmount(feeId);
    }

    @Override
    public void updateEnvFeePaymentAmount(String feeId) {
        envFeeMapper.updateEnvFeePaymentAmount(feeId);
    }
}


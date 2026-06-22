package com.zkhf.epmis.platform.envProtect.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envProtect.domain.OtherCertificate;
import com.zkhf.epmis.platform.envProtect.domain.OtherCertificateReq;
import com.zkhf.epmis.platform.envProtect.service.OtherCertificateService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.OtherCertificateMapper;
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
import java.util.List;

/**
 * 其他证书Service业务层处理
 */
@Slf4j
@Service
public class OtherCertificateServiceImpl implements OtherCertificateService {

    private OtherCertificateMapper otherCertificateMapper;

    @Autowired
    public void setOtherCertificateMapper(OtherCertificateMapper otherCertificateMapper) {
        this.otherCertificateMapper = otherCertificateMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectOtherCertificateList(OtherCertificateReq req) {
        if (null == req) {
            req = new OtherCertificateReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<OtherCertificate> list = otherCertificateMapper.selectOtherCertificateList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "其他证书", businessType = BusinessType.EXPORT)
    public void exportOtherCertificate(OtherCertificateReq req, HttpServletResponse response) {
        if (null == req) {
            req = new OtherCertificateReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("其他证书模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<OtherCertificate> list = otherCertificateMapper.selectOtherCertificateList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (OtherCertificate cert : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 证书名称
                    CellUtils.setStringVal(row, cellIndex++, cert.getCertName(), style);
                    // 发证机构
                    CellUtils.setStringVal(row, cellIndex++, cert.getIssueOffice(), style);
                    // 归属
                    CellUtils.setStringVal(row, cellIndex++, cert.getCertBelong(), style);
                    // 归属类型
                    String belongTypeName = null;
                    if (null != cert.getBelongType()) {
                        if (1 == cert.getBelongType()) {
                            belongTypeName = "机构";
                        } else if (2 == cert.getBelongType()) {
                            belongTypeName = "个人";
                        }
                    }
                    CellUtils.setStringVal(row, cellIndex++, belongTypeName, style);
                    // 有效日期
                    if (null != cert.getBeginDate() && null != cert.getEndDate()) {
                        CellUtils.setStringVal(row, cellIndex++, cert.getBeginDate().toString() + "至" + cert.getEndDate().toString(), style);
                    } else {
                        CellUtils.setStringVal(row, cellIndex++, null, style);
                    }
                    // 发证日期
                    CellUtils.setLocalDateStr(row, cellIndex++, cert.getIssueDate(), DateUtils.yy_m_d, style);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, cert.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("其他证书列表.xlsx", StandardCharsets.UTF_8));
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
    @Log(title = "其他证书", businessType = BusinessType.INSERT)
    public AjaxResult insertOtherCertificate(OtherCertificate info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setOtherId(UlidCreator.getMonotonicUlid().toString());
        int count = otherCertificateMapper.insertOtherCertificate(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(info.getOtherId(), AnnexTypeEnum.otherCertificate.name(), info.getAnnexIds());
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "其他证书", businessType = BusinessType.UPDATE)
    public AjaxResult updateOtherCertificate(OtherCertificate info) {
        int count = otherCertificateMapper.updateOtherCertificate(info);
        if (count > 0) {
            annexService.updateAnnex(info.getOtherId(), AnnexTypeEnum.otherCertificate.name(), info.getAnnexIds());
        }
        return AjaxResult.success();
    }

    @Override
    @Log(title = "其他证书", businessType = BusinessType.DELETE)
    public AjaxResult deleteOtherCertificateById(String otherId) {
        int count = otherCertificateMapper.deleteOtherCertificateById(otherId);
        if (count > 0) {
            // 删除附件
            annexService.updateAnnex(otherId, AnnexTypeEnum.otherCertificate.name(), null);
        }
        return AjaxResult.success(count);
    }
}


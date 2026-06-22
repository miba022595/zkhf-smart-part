package com.zkhf.epmis.platform.envProtect.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.envProtect.domain.EnvProPerson;
import com.zkhf.epmis.platform.envProtect.domain.EnvProPersonReq;
import com.zkhf.epmis.platform.envProtect.service.EnvProPersonService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.EnvProPersonMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 企业环保人员Service业务层处理
 */
@Slf4j
@Service
public class EnvProPersonServiceImpl implements EnvProPersonService {

    private EnvProPersonMapper envProPersonMapper;

    @Autowired
    public void setEnvProPersonMapper(EnvProPersonMapper envProPersonMapper) {
        this.envProPersonMapper = envProPersonMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public AjaxResult selectProPersonByEnt(String entCode) {
        return AjaxResult.success(envProPersonMapper.selectProPersonByEnt(entCode));
    }

    @Override
    public AjaxResult selectProPersonById(String proPersonId) {
        EnvProPerson info = envProPersonMapper.selectProPersonById(proPersonId);
        if (null == info) {
            return AjaxResult.error("企业环保人员信息为空");
        }
        // 判断在职离职
        fillPoll(Collections.singletonList(info), LocalDate.now());
        info.setAnnexInfoList(annexService.selectAnnexList(proPersonId, AnnexTypeEnum.entEnvProPerson.name()));
        return AjaxResult.success(info);
    }

    @Override
    public AjaxResult selectProPersonList(EnvProPersonReq req) {
        if (null == req) {
            req = new EnvProPersonReq();
        }
        // 判断权限，非admin账号只能查自己企业的信息
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EnvProPerson> list = envProPersonMapper.selectProPersonList(req);
        // 判断在职离职
        fillPoll(list, req.getNow());
        return PageUtils.getAjaxResult(list, true);
    }

    private void fillPoll(List<EnvProPerson> list, LocalDate now) {
        if (null == list || list.isEmpty()) {
            return;
        }
        list.forEach(e -> {
            if (null == e.getEntryDate() || e.getEntryDate().isAfter(now)) {
                // 未入职：入职开始时间为空，或者在当前时间之后
                e.setStatus(2);
            } else {
                if (null == e.getResignDate() || e.getResignDate().isAfter(now)) {
                    // 在职：离职时间为空，或离职时间在当前时间之后
                    e.setStatus(1);
                } else {
                    // 离职
                    e.setStatus(0);
                }
            }
        });
    }

    @Override
    @Log(title = "企业环保人员", businessType = BusinessType.EXPORT)
    public void exportProPerson(EnvProPersonReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnvProPersonReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保人员列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 判断权限，非admin账号只能查自己企业的信息
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EnvProPerson> list = envProPersonMapper.selectProPersonList(req);
            if (null != list && !list.isEmpty()) {
                // 判断在职离职
                fillPoll(list, req.getNow());
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnvProPerson person : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 环保人员姓名
                    CellUtils.setStringVal(row, cellIndex++, person.getProName(), style);
                    // 环保人员编号
                    CellUtils.setStringVal(row, cellIndex++, person.getProCode(), style);
                    // 性别
                    String sexName = null;
                    if (null != person.getProSex()) {
                        if (0 == person.getProSex()) {
                            sexName = "男";
                        } else if (1 == person.getProSex()) {
                            sexName = "女";
                        }
                    }
                    CellUtils.setStringVal(row, cellIndex++, sexName, style);
                    // 出生日期
                    CellUtils.setLocalDateStr(row, cellIndex++, person.getBirthDate(), DateUtils.yy_m_d, style);
                    // 联系电话
                    CellUtils.setStringVal(row, cellIndex++, person.getTelPhone(), style);
                    // 职称/证书
                    CellUtils.setStringVal(row, cellIndex++, person.getProTitle(), style);
                    // 岗位
                    CellUtils.setStringVal(row, cellIndex++, person.getProPost(), style);
                    // 住址
                    CellUtils.setStringVal(row, cellIndex++, person.getAddress(), style);
                    // 入职时间
                    CellUtils.setLocalDateStr(row, cellIndex++, person.getEntryDate(), DateUtils.yy_m_d, style);
                    // 离职时间
                    CellUtils.setLocalDateStr(row, cellIndex++, person.getResignDate(), DateUtils.yy_m_d, style);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, person.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保人员列表.xlsx", StandardCharsets.UTF_8));
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
    @Log(title = "企业环保人员模板", businessType = BusinessType.EXPORT)
    public void downloadTemplate(HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("环保人员列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex;// 从第5行开始插入

            rowIndex = 2;// 首行

            CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
            Row row;
            Cell cell;
            sheet.shiftRows(rowIndex, sheet.getLastRowNum(), 1);
            row = sheet.createRow(rowIndex);

            int cellIndex = 0;
            cell = CellUtils.getCell(row, cellIndex++, style);
            cell.setCellValue(1);
            setDefaultVal(row, cellIndex++, "xxx", style);
            setDefaultVal(row, cellIndex++, "xxx", style);
            setDefaultVal(row, cellIndex++, "男", style);
            setDefaultVal(row, cellIndex++, LocalDate.now().toString(), style);
            setDefaultVal(row, cellIndex++, "1234567889", style);
            setDefaultVal(row, cellIndex++, "xxx", style);
            setDefaultVal(row, cellIndex++, "xxx", style);
            setDefaultVal(row, cellIndex++, "xxx", style);
            setDefaultVal(row, cellIndex++, LocalDate.now().toString(), style);
            setDefaultVal(row, cellIndex++, LocalDate.now().toString(), style);
            setDefaultVal(row, cellIndex, "xxx", style);

            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("环保人员列表模板.xlsx", StandardCharsets.UTF_8));
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

    private void setDefaultVal(Row row, int index, String val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        cell.setCellValue(val);
    }

    @Override
    @Log(title = "企业环保人员模板", businessType = BusinessType.INSERT)
    public AjaxResult importTemplate(MultipartFile file, String entCode) {
        if (StringUtils.isEmpty(entCode)) {
            return AjaxResult.error("未指定归属企业");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(fileName)) {
            return AjaxResult.error("导入文件名称为空");
        }
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!".xlsx".equals(fileType)) {
            return AjaxResult.error("不支持 " + fileType + " 文件类型");
        }
        List<EnvProPerson> infos = new ArrayList<>();
        try {
            Workbook wb = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = wb.getSheetAt(0);
            // 过滤表头行，第一、二行
            List<String> errs = new ArrayList<>();
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                // 获取当前行的数据
                Row row = sheet.getRow(i);
                if (null == row)
                    continue;
                int index = 1;// 略过序号列
                EnvProPerson info = new EnvProPerson();
                info.setEntCode(entCode);
                info.setProPersonId(UlidCreator.getMonotonicUlid().toString());

                String value = CellUtils.getCellStringVal(row, index++);
                if (null == value) {
                    // 人员姓名为略过
                    continue;
                } else {
                    info.setProName(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null == value) {
                    errs.add("第" + (i + 1) + "行员工编号为空");
                } else {
                    info.setProCode(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if ("男".equals(value)) {
                    info.setProSex(0);
                } else if ("女".equals(value)) {
                    info.setProSex(1);
                }
                value = CellUtils.getCellStringDateVal(row, index++);
                if (null != value) {
                    LocalDate dateTime = strToLocalDate(value);
                    if (null == dateTime) {
                        errs.add("第" + (i + 1) + "行出生日期格式错误");
                    } else {
                        info.setBirthDate(dateTime);
                    }
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null == value) {
                    errs.add("第" + (i + 1) + "行联系电话为空");
                } else {
                    info.setTelPhone(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null != value) {
                    info.setProTitle(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null != value) {
                    info.setProPost(value);
                }
                value = CellUtils.getCellStringVal(row, index++);
                if (null != value) {
                    info.setAddress(value);
                }
                value = CellUtils.getCellStringDateVal(row, index++);
                if (null != value) {
                    LocalDate dateTime = strToLocalDate(value);
                    if (null == dateTime) {
                        errs.add("第" + (i + 1) + "行入职时间格式错误");
                    } else {
                        info.setEntryDate(dateTime);
                    }
                }
                value = CellUtils.getCellStringDateVal(row, index++);
                if (null != value) {
                    LocalDate dateTime = strToLocalDate(value);
                    if (null == dateTime) {
                        errs.add("第" + (i + 1) + "行离职时间格式错误");
                    } else {
                        info.setResignDate(dateTime);
                    }
                }
                value = CellUtils.getCellStringVal(row, index);
                if (null != value) {
                    info.setRemark(value);
                }
                infos.add(info);
            }
            if (!errs.isEmpty()) {
                return AjaxResult.error("文件内容异常，请检查", errs);
            }
            // 插入数据
            if (!infos.isEmpty()) {
                envProPersonMapper.batchInsertProPerson(infos);
            }
        } catch (Exception e) {
            return AjaxResult.error("导入文件解析失败", e.getMessage());
        }
        return AjaxResult.success(infos);
    }

    private LocalDate strToLocalDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Log(title = "企业环保人员", businessType = BusinessType.INSERT)
    public AjaxResult insertProPerson(EnvProPerson info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        info.setProPersonId(UlidCreator.getMonotonicUlid().toString());
        int count = envProPersonMapper.insertProPerson(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(info.getProPersonId(), AnnexTypeEnum.entEnvProPerson.name(), info.getAnnexIds());
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "企业环保人员", businessType = BusinessType.UPDATE)
    public AjaxResult updateProPerson(EnvProPerson info) {
        int count = envProPersonMapper.updateProPerson(info);
        if (count > 0) {
            annexService.updateAnnex(info.getProPersonId(), AnnexTypeEnum.entEnvProPerson.name(), info.getAnnexIds());
        }
        return AjaxResult.success();
    }

    @Override
    @Log(title = "企业环保人员", businessType = BusinessType.DELETE)
    public AjaxResult deleteProPersonByIds(List<String> ids) {
        if (null == ids || ids.isEmpty()) {
            return AjaxResult.error("请求信息为空");
        }
        int count = envProPersonMapper.deleteProPersonByIds(ids);
        if (count > 0) {
            // 删除附件
            ids.forEach(e -> annexService.updateAnnex(e, AnnexTypeEnum.entEnvProPerson.name(), null));
        }
        return AjaxResult.success(count);
    }
}


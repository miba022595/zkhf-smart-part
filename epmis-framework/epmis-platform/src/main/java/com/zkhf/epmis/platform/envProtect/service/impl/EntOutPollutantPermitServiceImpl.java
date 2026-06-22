package com.zkhf.epmis.platform.envProtect.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.PollutantCode;
import com.zkhf.epmis.platform.base.service.DictService;
import com.zkhf.epmis.platform.base.service.PollutantCodeService;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermit;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitCount;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitCountReq;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitReq;
import com.zkhf.epmis.platform.envProtect.service.EntOutPollutantPermitService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.EntOutPollutantPermitMapper;
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
import java.time.LocalDateTime;
import java.util.*;

/**
 * 企业排污许可基础Service业务层处理
 */
@Slf4j
@Service
public class EntOutPollutantPermitServiceImpl implements EntOutPollutantPermitService {

    private EntOutPollutantPermitMapper entOutPollutantPermitMapper;

    @Autowired
    public void setEntOutPollutantPermitMapper(EntOutPollutantPermitMapper entOutPollutantPermitMapper) {
        this.entOutPollutantPermitMapper = entOutPollutantPermitMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private PollutantCodeService pollutantCodeService;
    @Autowired
    public void setPollutantCodeService(PollutantCodeService pollutantCodeService) {
        this.pollutantCodeService = pollutantCodeService;
    }

    private DictService dictService;
    @Autowired
    public void setDictService(DictService dictService) {
        this.dictService = dictService;
    }

    @Override
    public List<Map<String, Object>> selectAllEntOutPollutantPermitCount(Integer permitYear) {
        return entOutPollutantPermitMapper.selectAllEntOutPollutantPermitCount(permitYear);
    }

    @Override
    public AjaxResult selectEntOutPollutantPermitList(EntOutPollutantPermitReq req) {
        AjaxResult result;
        if (null == req) {
            req = new EntOutPollutantPermitReq();
        }
        // 判断权限，非admin账号只能查自己企业的信息
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EntOutPollutantPermit> list = entOutPollutantPermitMapper.selectEntOutPollutantPermitList(req);
        // 设置污染物信息
        fillPoll(list);
        result = PageUtils.getAjaxResult(list, true);
        return result;
    }

    private void fillPoll(List<EntOutPollutantPermit> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 获取所有污染物code和name的对应关系
        Map<String, PollutantCode> codeMap = pollutantCodeService.selectAllPollCodeMap();
        StringBuilder bu, unit;
        for (EntOutPollutantPermit p : list) {
            bu = new StringBuilder();
            unit = new StringBuilder();
            appendPollDetail(codeMap, bu, unit, p.getGasPollType());
            p.setGasPollDesc(bu.substring(1));
            p.setGasPollUnit(unit.substring(1));
            bu = new StringBuilder();
            unit = new StringBuilder();
            // 设置污染物的描述和单位
            appendPollDetail(codeMap, bu, unit, p.getWaterPollType());
            p.setWaterPollDesc(bu.substring(1));
            p.setWaterPollUnit(unit.substring(1));
        }
        /*
        获取信息
         poll_permit_cate: 排污许可管理类别
         emission_rule_gas: 废气排放规律
         emission_rule_water: 废水排放规律
         */
        List<String> dictTypes = Arrays.asList("poll_permit_cate", "emission_rule_gas", "emission_rule_water");
        List<DictData> dictList = dictService.getDataListByTypes(dictTypes);
        if (null != dictList && !dictList.isEmpty()) {
            /* dictLabel: "工程车辆"; dictType: "ent_product"; dictValue: "gccl" */
            Map<String, String> perMap = new HashMap<>();
            Map<String, String> gasMap = new HashMap<>();
            Map<String, String> waterMap = new HashMap<>();
            dictList.forEach(e -> {
                String dictType = e.getDictType();
                if ("poll_permit_cate".equals(dictType)) {
                    perMap.put(e.getDictValue(), e.getDictLabel());
                } else if ("emission_rule_gas".equals(dictType)) {
                    gasMap.put(e.getDictValue(), e.getDictLabel());
                } else if ("emission_rule_water".equals(dictType)) {
                    waterMap.put(e.getDictValue(), e.getDictLabel());
                }
            });
            for (EntOutPollutantPermit p : list) {
                p.setPermitLevelDesc(perMap.get(p.getPermitLevel()));
                p.setGasEmissionRuleDesc(gasMap.get(p.getGasEmissionRule()));
                p.setWaterEmissionRuleDesc(waterMap.get(p.getWaterEmissionRule()));
            }
        }
    }

    private void appendPollDetail(Map<String, PollutantCode> codeMap, StringBuilder bu, StringBuilder unit, String pollType) {
        if (null == pollType) {
            bu.append(",");
            unit.append(",");
            return;
        }
        for (String code : pollType.split(",")) {
            bu.append(",");
            unit.append(",");
            PollutantCode sub = codeMap.get(code);
            if (null != sub) {
                if (StringUtils.isNotEmpty(sub.getPollutantNameCn())) {
                    bu.append(sub.getPollutantNameCn());
                }
                if (StringUtils.isNotEmpty(sub.getUnitPfEn())) {
                    unit.append(sub.getUnitPfEn());
                }
            }
        }
    }

    @Override
    @Log(title = "企业排污许可基础", businessType = BusinessType.EXPORT)
    public void exportEntOutPollutantPermit(EntOutPollutantPermitReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntOutPollutantPermitReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("排污许可列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 判断权限，非admin账号只能查自己企业的信息
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntOutPollutantPermit> list = entOutPollutantPermitMapper.selectEntOutPollutantPermitList(req);
            if (null != list && !list.isEmpty()) {
                // 设置污染物信息
                fillPoll(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntOutPollutantPermit permit : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getEntName());
                    // 社会信用代码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getSocialCreditCode());
                    // 排污许可管理类别
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getPermitLevelDesc());
                    // 许可证编号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getPermitNum());
                    // 有效期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (StringUtils.isNotEmpty(permit.getBeginDate()) && StringUtils.isNotEmpty(permit.getEndDate())) {
                        cell.setCellValue(permit.getBeginDate() + "至" + permit.getEndDate());
                    } else {
                        cell.setCellValue("");
                    }
                    // 发证机关
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getIssueOffice());
                    // 发证日期
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getIssueDate());
                    // 执行报告报送要求
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getReportRequire());
                    // 主要产品
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getProductDesc());
                    // 产量
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getProductOutput());
                    // 废气污染物种类(pollutantCodes)
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getGasPollDesc());
                    // 废气排放规律
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getGasEmissionRuleDesc());
                    // 废气执行标准
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getGasExecuteStandard());
                    // 废气特征污染物
                    CellUtils.setStringVal(row, cellIndex++, permit.getGasCharPoll(), style);
                    // 废水污染物种类(pollutantCodes)
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getWaterPollDesc());
                    // 废水排放规律
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getWaterEmissionRuleDesc());
                    // 废水执行标准
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(permit.getWaterExecuteStandard());
                    // 特征污染物
                    CellUtils.setStringVal(row, cellIndex++, permit.getWaterCharPoll(), style);
                    // 备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(permit.getRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业排污许可信息.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
        }
    }

    @Override
    @Log(title = "企业排污许可基础", businessType = BusinessType.UPDATE)
    public AjaxResult updateEntOutPollutantPermit(EntOutPollutantPermit permit) {
        if (StringUtils.isEmpty(permit.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        permit.setUpdateTime(LocalDateTime.now());
        // 判断是否已存在
        int exist = entOutPollutantPermitMapper.checkExistEntOutPollutantPermit(permit.getEntCode());
        if (exist > 0) { // 存在时修改
            entOutPollutantPermitMapper.updateEntOutPollutantPermit(permit);
        } else {
            entOutPollutantPermitMapper.insertEntOutPollutantPermit(permit);
        }
        // 更新附件
        if (null != permit.getAnnexIds() && !permit.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(permit.getEntCode(), AnnexTypeEnum.entOutPollutantPermit.name(), permit.getAnnexIds());
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult selectEntOutPollutantPermitCountList(EntOutPollutantPermitCountReq req) {
        if (null == req) {
            req = new EntOutPollutantPermitCountReq();
        }
        // 设置企业编号
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        // 分页查询
        PageUtils.startPage();
        List<EntOutPollutantPermitCount> list = entOutPollutantPermitMapper.selectEntOutPollutantPermitCountList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "企业排污许可总量基础", businessType = BusinessType.EXPORT)
    public void exportEntOutPollutantPermitCount(EntOutPollutantPermitCountReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntOutPollutantPermitCountReq();
        }
        // 设置企业编号
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("排污许可列表许可总量模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<EntOutPollutantPermitCount> list = entOutPollutantPermitMapper.selectEntOutPollutantPermitCountList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 1;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntOutPollutantPermitCount count : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 年份
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(count.getPermitYear());
                    // 污染因子类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    if (null == count.getPollType()) {
                        cell.setCellValue("");
                    } else if (1 == count.getPollType()) {
                        cell.setCellValue("废水");
                    } else if (2 == count.getPollType()) {
                        cell.setCellValue("废气");
                    } else if (3 == count.getPollType()) {
                        cell.setCellValue("无组织");
                    }
                    // 污染因子
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(count.getPollutantNameCn());
                    // 许可总量（t/a）
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(null == count.getPermitCount() ? "" : Double.toString(count.getPermitCount()));
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业排污许可总量列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
        }
    }

    @Override
    @Log(title = "企业排污许可总量基础", businessType = BusinessType.INSERT)
    public AjaxResult insertEntOutPollutantPermitCount(EntOutPollutantPermitCount count) {
        if (StringUtils.isEmpty(count.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        int exists = entOutPollutantPermitMapper.existsEntOutPollutantPermitCount(count);
        if (exists > 0) {
            return AjaxResult.error("已存在相同的配置，请勿重复添加");
        }
        count.setPollPermitCountId(UlidCreator.getMonotonicUlid().toString());
        entOutPollutantPermitMapper.insertEntOutPollutantPermitCount(count);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "企业排污许可总量基础", businessType = BusinessType.UPDATE)
    public AjaxResult updateEntOutPollutantPermitCount(EntOutPollutantPermitCount count) {
        entOutPollutantPermitMapper.updateEntOutPollutantPermitCount(count);
        return AjaxResult.success();
    }

    @Override
    @Log(title = "企业排污许可总量基础", businessType = BusinessType.DELETE)
    public AjaxResult deleteEntOutPollutantPermitCountByPollPermitIds(List<String> pollPermitIds) {
        if (null == pollPermitIds || pollPermitIds.isEmpty()) {
            return AjaxResult.error("请求信息为空");
        }
        entOutPollutantPermitMapper.deleteEntOutPollutantPermitCountByPollPermitCountIds(pollPermitIds);
        return AjaxResult.success();
    }
}


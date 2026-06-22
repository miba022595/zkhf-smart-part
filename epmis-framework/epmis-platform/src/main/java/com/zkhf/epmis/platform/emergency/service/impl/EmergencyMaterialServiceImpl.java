package com.zkhf.epmis.platform.emergency.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterial;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterialReq;
import com.zkhf.epmis.platform.emergency.service.EmergencyMaterialService;
import com.zkhf.epmis.platform.ent.domain.Enterprise;
import com.zkhf.epmis.platform.ent.domain.EnterpriseReq;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.emergency.EmergencyMaterialMapper;
import com.zkhf.epmis.platform.mapper.ent.EnterpriseMapper;
import com.zkhf.epmis.platform.send.weixin.WeComSend;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmergencyMaterialServiceImpl implements EmergencyMaterialService {

    @Value("${emergency.material.warn.days:30}")
    private Integer warnDays;

    private EmergencyMaterialMapper emergencyMaterialMapper;
    @Autowired
    public void setEmergencyMaterialMapper(EmergencyMaterialMapper emergencyMaterialMapper) {
        this.emergencyMaterialMapper = emergencyMaterialMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private EnterpriseMapper enterpriseMapper;
    @Autowired
    public void setEnterpriseMapper(EnterpriseMapper enterpriseMapper) {
        this.enterpriseMapper = enterpriseMapper;
    }

    private WeComSend weComSend;
    @Autowired
    public void setWeComSend(WeComSend weComSend) {
        this.weComSend = weComSend;
    }

    @Override
    public AjaxResult list(EmergencyMaterialReq req) {
        // 请求参数转换
        req = initEmergencyMaterialReq(req);
        if (null == req) {
            return AjaxResult.success();
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EmergencyMaterial> list = emergencyMaterialMapper.selectList(req);
        // 填充附件信息
        fillAnnexInfo(list);
        return PageUtils.getAjaxResult(list, page);
    }

    private void fillAnnexInfo(List<EmergencyMaterial> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<String> sourceIds = list.stream().map(EmergencyMaterial::getMaterialId).toList();
        AnnexReq annexReq = new AnnexReq();
        annexReq.setSourceIds(sourceIds);
        annexReq.setSourceType(AnnexTypeEnum.emergencyMaterialPhoto.name());
        List<AnnexInfo> annexList = annexService.selectAnnexList(annexReq);
        if (null != annexList && !annexList.isEmpty()) {
            Map<String, List<AnnexInfo>> annexMap = new HashMap<>();
            for (AnnexInfo annex : annexList) {
                annexMap.computeIfAbsent(annex.getSourceId(), k -> new ArrayList<>()).add(annex);
            }
            for (EmergencyMaterial material : list) {
                material.setAnnexList(annexMap.get(material.getMaterialId()));
            }
        }
    }

    private EmergencyMaterialReq initEmergencyMaterialReq(EmergencyMaterialReq req) {
        if (null == req) {
            req = new EmergencyMaterialReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            List<String> authEntCodes = GVarContainer.getEntCodes();
            if (null == authEntCodes || authEntCodes.isEmpty()) {
                return null;
            }
            if (StringUtils.isNotBlank(req.getEntCode())) {
                if (!authEntCodes.contains(req.getEntCode())) {
                    return null;
                }
            } else {
                req.setEntCodes(authEntCodes);
            }
        }
        return req;
    }

    @Override
    @Log(title = "应急物资", businessType = BusinessType.INSERT)
    public AjaxResult add(EmergencyMaterial info) {
        info.setMaterialId(UlidCreator.getMonotonicUlid().toString());
        info.setWarnStatus(calculateWarnStatus(info.getExpireDate(), info.getRecheckDate()));
        info.setWarnDays(warnDays);
        info.setWarnType(info.getWarnStatus() == 1 ? "expire" : "recheck");
        int rows = emergencyMaterialMapper.insert(info);
        if (rows > 0 && info.getAnnexIds() != null && info.getAnnexIds().size() > 0) {
            annexService.updateAnnex(info.getMaterialId(), AnnexTypeEnum.emergencyMaterialPhoto.name(), info.getAnnexIds());
        }
        return AjaxResult.success(rows);
    }

    @Override
    @Log(title = "应急物资", businessType = BusinessType.UPDATE)
    public AjaxResult update(EmergencyMaterial info) {
        EmergencyMaterial material = emergencyMaterialMapper.selectById(info.getMaterialId());
        if (material == null) {
            return AjaxResult.error("数据不存在");
        }
        info.setWarnStatus(calculateWarnStatus(info.getExpireDate(), info.getRecheckDate()));
        info.setWarnType(info.getWarnStatus() == 1 ? "expire" : "recheck");
        int rows = emergencyMaterialMapper.update(info);
        if (rows > 0) {
            annexService.updateAnnex(info.getMaterialId(), AnnexTypeEnum.emergencyMaterialPhoto.name(), info.getAnnexIds());
        }
        return AjaxResult.success(rows);
    }

    @Override
    @Log(title = "应急物资", businessType = BusinessType.DELETE)
    public AjaxResult delete(EmergencyMaterial info) {
        int rows = emergencyMaterialMapper.deleteById(info.getMaterialId());
        if (rows > 0) {
            annexService.updateAnnex(info.getMaterialId(), AnnexTypeEnum.emergencyMaterialPhoto.name(), null);
        }
        return AjaxResult.success(rows);
    }

    @Override
    public AjaxResult getWarnList() {
        AjaxResult result = AjaxResult.success();
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
        }
        List<EmergencyMaterial> list = emergencyMaterialMapper.selectWarnList(entCodes);
        if (null != list) {
            result.put("total", list.size());
        }
        result.put("data", list);
        return result;
    }

    @Override
    @Log(title = "应急物资", businessType = BusinessType.EXPORT)
    public void export(HttpServletResponse response, EmergencyMaterialReq req) {
        req = initEmergencyMaterialReq(req);
        if (req == null) {
            return;
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("应急物资台账模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<EmergencyMaterial> list = emergencyMaterialMapper.selectList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3; // 首行

                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle style0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                CellStyle style4 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 4);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EmergencyMaterial material : list) {
                    row = sheet.createRow(rowIndex);
                    rowIndex++;

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style0);
                    // 物资名称
                    CellUtils.setStringVal(row, cellIndex++, material.getMaterialName(), style);
                    // 型号规格
                    CellUtils.setStringVal(row, cellIndex++, material.getModelSpec(), style);
                    // 存放地点
                    CellUtils.setStringVal(row, cellIndex++, material.getStorePlace(), style);
                    // 数量
                    CellUtils.setDoubleVal(row, cellIndex++, material.getQuantity(), style4);
                    // 单位
                    CellUtils.setStringVal(row, cellIndex++, material.getUnit(), style);
                    // 保质期
                    CellUtils.setLocalDateStr(row, cellIndex++, material.getExpireDate(), DateUtils.yy_m_d, style);
                    // 复检日期
                    CellUtils.setLocalDateStr(row, cellIndex++, material.getRecheckDate(), DateUtils.yy_m_d, style);
                    // 生产厂家
                    CellUtils.setStringVal(row, cellIndex++, material.getManufacturerName(), style);
                    // 厂家联系人
                    CellUtils.setStringVal(row, cellIndex++, material.getManufacturerContact(), style);
                    // 厂家电话
                    CellUtils.setStringVal(row, cellIndex++, material.getManufacturerPhone(), style);
                    // 管理员
                    CellUtils.setStringVal(row, cellIndex++, material.getManagerName(), style);
                    // 管理员电话
                    CellUtils.setStringVal(row, cellIndex++, material.getManagerPhone(), style);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, material.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("应急物资台账.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("导出应急物资台账失败", e);
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
    public void scanWarnStatus() {
        List<EmergencyMaterial> list = emergencyMaterialMapper.selectAll();
        if (list == null || list.isEmpty()) {
            return;
        }
        for (EmergencyMaterial material : list) {
            Integer oldStatus = material.getWarnStatus();
            Integer newStatus = calculateWarnStatus(material.getExpireDate(), material.getRecheckDate());
            if (!newStatus.equals(oldStatus)) {
                material.setWarnStatus(newStatus);
                material.setWarnType(newStatus == 1 ? "expire" : "recheck");
                emergencyMaterialMapper.update(material);
                
                // 如果状态变为 1(已过期) 或 2(即将过期)，发送通知
                if (newStatus > 0) {
                    sendExpiryNotice(material);
                }
            }
        }
    }

    private void sendExpiryNotice(EmergencyMaterial material) {
        String title = "应急物资质保到期预警";
        String content = String.format("物资名称：%s\n存放地点：%s\n状态：%s", 
            material.getMaterialName(), 
            material.getStorePlace(), 
            material.getWarnStatus() == 1 ? "已过期" : "即将过期");
        
        // 发送给物资管理员（如果有配置）
        if (StringUtils.isNotEmpty(material.getManagerPhone())) {
            // 这里可以调用短信或企业微信通知
            log.info("Sending expiry notice for material: {}, phone: {}", material.getMaterialName(), material.getManagerPhone());
        }
        
        // 发送给企业关联的企业微信部门
        if (StringUtils.isNotEmpty(material.getEntCode())) {
            EnterpriseReq entReq = new EnterpriseReq();
            entReq.setEntCode(material.getEntCode());
            List<Enterprise> entList = enterpriseMapper.selectList(entReq);
            if (entList != null && !entList.isEmpty()) {
                Enterprise ent = entList.get(0);
                if (StringUtils.isNotEmpty(ent.getWeComMsg())) {
                    weComSend.sendWXMessageToPartyWithCard(ent.getWeComMsg(), title, content, "URL");
                }
            }
        }
    }

    private int calculateWarnStatus(LocalDate expireDate, LocalDate recheckDate) {
        LocalDate today = LocalDate.now();
        if (expireDate != null) {
            long daysUntilExpire = ChronoUnit.DAYS.between(today, expireDate);
            if (daysUntilExpire < 0) {
                return 2;
            } else if (daysUntilExpire <= warnDays) {
                return 1;
            }
        }
        if (recheckDate != null) {
            long daysUntilRecheck = ChronoUnit.DAYS.between(today, recheckDate);
            if (daysUntilRecheck < 0) {
                return 2;
            } else if (daysUntilRecheck <= warnDays) {
                return 1;
            }
        }
        return 0;
    }
}


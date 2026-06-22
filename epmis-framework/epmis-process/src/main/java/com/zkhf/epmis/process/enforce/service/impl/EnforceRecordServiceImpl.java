package com.zkhf.epmis.process.enforce.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.enforce.domain.EnforceRecord;
import com.zkhf.epmis.process.enforce.domain.EnforceRecordReq;
import com.zkhf.epmis.process.enforce.service.EnforceRecordService;
import com.zkhf.epmis.process.facade.auth.AuthFacade;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.enforce.EnforceRecordMapper;
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
 * 执法检查记录Service业务层处理
 */
@Slf4j
@Service
public class EnforceRecordServiceImpl implements EnforceRecordService {

    private EnforceRecordMapper enforceRecordMapper;
    @Autowired
    public void setEnforceRecordMapper(EnforceRecordMapper enforceRecordMapper) {
        this.enforceRecordMapper = enforceRecordMapper;
    }

    private AuthFacade authFacade;
    @Autowired
    public void setAuthFacade(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectEnforceRecordList(EnforceRecordReq req) {
        if (req == null) {
            req = new EnforceRecordReq();
        }
        Map<String, String> entMap = new HashMap<>();
        Map<String, OutPutInfo> outMap = new HashMap<>();
        String deal = reqDeal(req, entMap, outMap);
        List<EnforceRecord> list;
        boolean page = false;
        if (StringUtils.isNotEmpty(deal)) {
            log.error("检查失败 {}", deal);
            list = new ArrayList<>();
        } else {
            page = PageUtils.startPageCheckExists();
            list = enforceRecordMapper.selectEnforceRecordList(req);
        }
        // 填充信息
        fill(entMap, outMap, list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "执法检查记录", businessType = BusinessType.EXPORT)
    public void exportEnforceRecord(EnforceRecordReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EnforceRecordReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("执法检查列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            Map<String, String> entMap = new HashMap<>();
            Map<String, OutPutInfo> outMap = new HashMap<>();
            String deal = reqDeal(req, entMap, outMap);
            List<EnforceRecord> list;
            if (StringUtils.isNotEmpty(deal)) {
                list = new ArrayList<>();
            } else {
                list = enforceRecordMapper.selectEnforceRecordList(req);
            }
            // 填充信息
            fill(entMap, outMap, list);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 2; // 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EnforceRecord info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, styleN0);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, info.getEntName(), style);
                    // 排口编码
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutCode(), style);
                    // 排口名称
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutName(), style);
                    // 检查单位名称
                    CellUtils.setStringVal(row, cellIndex++, info.getUnitName(), style);
                    // 检查人
                    CellUtils.setStringVal(row, cellIndex++, info.getInspector(), style);
                    // 运维单位
                    CellUtils.setStringVal(row, cellIndex++, info.getOpsUnitName(), style);
                    // 运维人员
                    CellUtils.setStringVal(row, cellIndex++, info.getOpsUserName(), style);
                    // 检查时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex++, info.getCheckDate(), DateUtils.yy_m_d_h_m_s, style);
                    // 检查原因
                    CellUtils.setStringVal(row, cellIndex++, info.getCheckReason(), style);
                    // 检查结果是否合格（1：合格，0：不合格/异常）
                    CellUtils.setStringVal(row, cellIndex++, checkPass.equals(info.getCheckFlag()) ? "合格" : "不合格/异常", style);
                    // 检查结论
                    CellUtils.setStringVal(row, cellIndex++, info.getCheckConclusion(), style);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, info.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("执法检查列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(Map<String, String> entMap, Map<String, OutPutInfo> outMap, List<EnforceRecord> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<SysUser> allUser = authFacade.allUserInfo();
        Map<Long, String> userMap = new HashMap<>();
        if (null != allUser) {
            allUser.forEach( u -> {
                if (null != u.getUserId()) {
                    if (StringUtils.isEmpty(u.getNickName())) {
                        userMap.put(u.getUserId(), u.getUserName());
                    } else {
                        userMap.put(u.getUserId(), u.getNickName());
                    }
                }
            });
        }
        List<Map<String, Object>> extList = platformFacade.allExtUnitList();
        Map<String, String> extMap = new HashMap<>();
        if (null != extList && !extList.isEmpty()) {
            extList.forEach( u -> {
                String unitCode = MapUtil.getStr(u, "unitCode");
                if (StringUtils.isNotEmpty(unitCode)) {
                    extMap.put(unitCode, MapUtil.getStr(u, "unitName"));
                }
            });
        }
        for (EnforceRecord info : list) {
            if (entMap.containsKey(info.getEntCode())) {
                info.setEntName(entMap.get(info.getEntCode()));
            }
            OutPutInfo out = outMap.get(info.getOutPutId());
            if (null != out) {
                info.setOutPutCode(out.getOutPutCode());
                info.setOutPutName(out.getOutPutName());
            }
            if (userMap.containsKey(info.getOpsUser())) {
                info.setOpsUserName(userMap.get(info.getOpsUser()));
            }
            if (extMap.containsKey(info.getOpsUnit())) {
                info.setOpsUnitName(extMap.get(info.getOpsUnit()));
            }
        }

    }

    private String reqDeal(EnforceRecordReq req, Map<String, String> entMap, Map<String, OutPutInfo> outMap) {
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return "企业权限列表为空";
            }
            if (StringUtils.isNotEmpty(req.getEntCode()) && !req.getEntCodes().contains(req.getEntCode())) {
                return "无该企业权限";
            }
        }
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        for (OutPutInfo info : outPutList) {
            if (StringUtils.isEmpty(info.getEntCode())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getEntCode()) && !req.getEntCode().equals(info.getEntCode())) {
                continue;
            }
            if (null != req.getEntCodes() && !req.getEntCodes().contains(info.getEntCode())) {
                continue;
            }
            if (StringUtils.isNotEmpty(req.getRegion())) {// 地区匹配
                if (StringUtils.isEmpty(info.getRegion())) {
                    continue;
                }
                if (!info.getRegion().equals(req.getRegion()) && !info.getRegion().startsWith(req.getRegion() + ",")) {
                    continue;
                }
            }
            if (StringUtils.isNotEmpty(req.getOutPutId()) && !req.getOutPutId().equals(info.getOutPutId())) {
                continue;
            }
            entMap.put(info.getEntCode(), info.getEntName());
            outMap.put(info.getOutPutId(), info);
        }
        if (entMap.isEmpty()) {
            return "无企业权限";
        }
        if (outMap.isEmpty()) {
            return "无排口权限";
        }
        return null;
    }

    @Override
    @Log(title = "执法检查记录", businessType = BusinessType.INSERT)
    public AjaxResult insertEnforceRecord(EnforceRecord info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("配置参数不能为空");
        }
        // 未设置运维人员就按登录账户来
        if (null == info.getOpsUser()) {
            info.setOpsUser(GVarContainer.getUserId());
        }
        // 检查结果不是合格就是不合格
        if (!checkPass.equals(info.getCheckFlag())) {
            info.setCheckFlag(checkErr);
        }
        int count = enforceRecordMapper.insertEnforceRecord(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            updateAnnex(info.getId(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "执法检查记录", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnforceRecord(EnforceRecord info) {
        EnforceRecord old = enforceRecordMapper.selectEnforceRecordById(info.getId());
        if (null == old) {
            return AjaxResult.error("未知的监测记录");
        }
        Long loginId = GVarContainer.getUserId();
        if (null == old.getOpsUser()) {
            old.setOpsUser(GVarContainer.getUserId());
        } else if (!old.getOpsUser().equals(loginId)) {
            // 谁加的谁改
            return AjaxResult.error("无权限修改");
        }
        // 检查结果不是合格就是不合格
        if (!checkPass.equals(info.getCheckFlag())) {
            info.setCheckFlag(checkErr);
        }
        int count = enforceRecordMapper.updateEnforceRecord(info);
        if (count > 0) {
            updateAnnex(info.getId(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "执法检查记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnforceRecord(EnforceRecord info) {
        int count = enforceRecordMapper.deleteEnforceRecordById(info.getId());
        if (count > 0) {
            updateAnnex(info.getId(), null);
        }
        return AjaxResult.success(count);
    }

    private void updateAnnex(Long recordId, List<String> annexIds) {
        JSONObject req = new JSONObject();
        req.set("sourceId", recordId);
        req.set("sourceType", AnnexTypeEnum.enforceRecord.name);
        req.set("annexIds", annexIds);
        platformFacade.updateAnnex(req);
    }
}


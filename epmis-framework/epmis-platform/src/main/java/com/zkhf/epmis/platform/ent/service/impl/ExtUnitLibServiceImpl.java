package com.zkhf.epmis.platform.ent.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.ent.domain.ExtUnit;
import com.zkhf.epmis.platform.ent.domain.ExtUnitReq;
import com.zkhf.epmis.platform.ent.service.ExtUnitLibService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.ExtUnitLibMapper;
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
 * 第三方单位库Service业务层处理
 */
@Slf4j
@Service
public class ExtUnitLibServiceImpl implements ExtUnitLibService {

    private ExtUnitLibMapper extUnitLibMapper;
    @Autowired
    public void setExtUnitLibMapper(ExtUnitLibMapper extUnitLibMapper) {
        this.extUnitLibMapper = extUnitLibMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public List<ExtUnit> selectAllExtUnitList() {
        return extUnitLibMapper.selectExtUnitList(null);
    }

    @Override
    public AjaxResult selectExtUnitList(ExtUnitReq req) {
        if (null == req) {
            req = new ExtUnitReq();
        }
        // 添加权限
        req.setUserId(GVarContainer.getUserId());
        boolean page = PageUtils.startPageCheckExists();
        List<ExtUnit> list = extUnitLibMapper.selectExtUnitList(req);
        // 其他的信息设置
        fill(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public void exportExtUnit(ExtUnitReq req, HttpServletResponse response) {
        if (null == req) {
            req = new ExtUnitReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("第三方单位列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            req.setUserId(GVarContainer.getUserId());
            List<ExtUnit> list = extUnitLibMapper.selectExtUnitList(req);
            if (null != list && !list.isEmpty()) {
                // 其他的信息设置
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行

                // 获取数字单元格格式
                CellStyle numStyle = CellUtils.getCellStyle(workbook, sheet, rowIndex,0,0);
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (ExtUnit info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, style);
                    // 统一社会信用代码
                    CellUtils.setStringVal(row, cellIndex++, info.getUnitCode(), style);
                    // 单位名称
                    CellUtils.setStringVal(row, cellIndex++, info.getUnitName(), style);
                    // 联系人
                    CellUtils.setStringVal(row, cellIndex++, info.getContactPerson(), style);
                    // 联系电话
                    CellUtils.setStringVal(row, cellIndex++, info.getContactNumber(), style);
                    // 缴纳社保人数
                    CellUtils.setIntegerVal(row, cellIndex++, info.getSocialNum(), numStyle);
                    // 注册地址
                    CellUtils.setStringVal(row, cellIndex++, info.getRegisterAddr(), style);
                    // 资质开始日期
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getCertStart(), DateUtils.yy_m_d, style);
                    // 资质结束日期
                    CellUtils.setLocalDateStr(row, cellIndex++, info.getCertEnd(), DateUtils.yy_m_d, style);
                    // 服务内容
                    CellUtils.setStringVal(row, cellIndex, info.getServItem(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("第三方单位列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<ExtUnit> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        list.forEach( e -> {
            if (null != e.getExtraInfoStr()) {
                e.setExtraInfo(JSONObject.parse(e.getExtraInfoStr()));
            }
        });
    }

    @Override
    public AjaxResult insertExtUnit(ExtUnit info) {
        // 查看第三方的社会信用代码是否已存在
        int count = extUnitLibMapper.checkExistsUnitCode(info.getUnitCode());
        if (count > 0) {
            return AjaxResult.error("第三方单位已存在");
        }
        info.setExtraInfoStr(null == info.getExtraInfo() ? "{}" : info.getExtraInfo().toJSONString());
        count = extUnitLibMapper.insertExtUnit(info);
        if (count > 0) {
            // 设置附件
            if (info.getAnnexIds() != null && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getUnitCode(), AnnexTypeEnum.extUnitType.name, info.getAnnexIds());
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult updateExtUnit(ExtUnit info){
        int count = extUnitLibMapper.updateExtUnit(info);
        // 修改附件信息
        if (count > 0) {
            annexService.updateAnnex(info.getUnitCode(), AnnexTypeEnum.extUnitType.name, info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult editExtUser(JSONObject req){
        Long userId = req.getLong("userId");
        if (null == userId) {
            return AjaxResult.success(0);
        }
        // 删除旧的关联关系
        extUnitLibMapper.deleteUnitUser(userId);
        List<String> unitCodes = req.getList("unitCodes", String.class);
        if (null == unitCodes || unitCodes.isEmpty()) {
            return AjaxResult.success(0);
        }
        // 添加新的关联关系
        int count = extUnitLibMapper.insertUnitUser(userId, unitCodes);
        return AjaxResult.success(count);
    }
}

